import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import {
  IonContent, IonHeader, IonTitle, IonToolbar, IonList, IonItem, IonLabel, IonNote,
  IonButton, IonIcon, IonBadge, IonSpinner, IonInput, IonSegment, IonSegmentButton,
  IonButtons, IonBackButton,
  ToastController,
} from '@ionic/angular';
import { addIcons } from 'ionicons';
import {
  checkmarkCircleOutline, cardOutline, lockClosedOutline, qrCodeOutline, copyOutline, timeOutline,
  closeCircleOutline, ellipseOutline,
} from 'ionicons/icons';
import { PedidoService } from '../../services/pedido.service';
import { PagamentoService } from '../../services/pagamento.service';
import {
  COR_STATUS, ETAPAS_PEDIDO, PedidoResponse, STATUS_FINAIS, STATUS_LABEL, StatusPedido,
} from '../../models/pedido.model';
import { FormaCartao, PagamentoResponse } from '../../models/pagamento.model';
import { toDataURL } from 'qrcode';

/** Vencido ou distante demais (mesma regra do back: até 20 anos à frente) */
function validadeCartao(control: AbstractControl): ValidationErrors | null {
  const valor = control.value as string;
  if (!/^(0[1-9]|1[0-2])\/\d{2}$/.test(valor)) return null;   // o formato é com o Validators.pattern

  const [mes, ano] = valor.split('/').map(Number);
  const hoje = new Date();
  // Converte para "meses desde o ano 0", para comparar com um número só
  const validade = (2000 + ano) * 12 + (mes - 1);
  const atual = hoje.getFullYear() * 12 + hoje.getMonth();

  if (validade < atual) return { vencido: true };
  if (validade > atual + 20 * 12) return { distante: true };
  return null;
}

/** B4: 16 dígitos e dígito verificador correto (Luhn, a mesma regra do ValidadorCartao do back) */
function numeroCartao(control: AbstractControl): ValidationErrors | null {
  const digitos = String(control.value ?? '').replace(/\D/g, '');
  if (!digitos) return null;   // o vazio é com o Validators.required
  if (digitos.length !== 16) return { numeroCartao: true };

  let soma = 0;
  let dobrar = false;
  for (let i = digitos.length - 1; i >= 0; i--) {
    let d = Number(digitos[i]);
    if (dobrar) {
      d *= 2;
      if (d > 9) d -= 9;
    }
    soma += d;
    dobrar = !dobrar;
  }
  return soma % 10 === 0 ? null : { numeroCartao: true };
}

@Component({
  selector: 'app-pedido-confirmacao',
  templateUrl: './pedido-confirmacao.page.html',
  styleUrls: ['./pedido-confirmacao.page.scss'],
  imports: [
    CurrencyPipe, RouterLink, ReactiveFormsModule,
    IonContent, IonHeader, IonTitle, IonToolbar, IonList, IonItem, IonLabel, IonNote,
    IonButton, IonIcon, IonBadge, IonSpinner, IonInput, IonSegment, IonSegmentButton,
    IonButtons, IonBackButton,
  ],
})
export class PedidoConfirmacaoPage implements OnInit {
  private route = inject(ActivatedRoute);
  private pedidoService = inject(PedidoService);
  private pagamentoService = inject(PagamentoService);
  private toast = inject(ToastController);
  private fb = inject(FormBuilder);
  private destroyRef = inject(DestroyRef);
  qrCode = signal<string | null>(null);
  readonly statusLabel = STATUS_LABEL;
  pedido = signal<PedidoResponse | null>(null);
  erro = signal(false);

  readonly etapas = ETAPAS_PEDIDO;
  readonly corStatus = COR_STATUS;

  /** Posição do status atual na timeline (-1 se cancelado) */
  indiceAtual = computed(() => {
    const status = this.pedido()?.status;
    return status ? ETAPAS_PEDIDO.indexOf(status) : -1;
  });

  cancelado = computed(() => this.pedido()?.status === 'CANCELADO');

  // ---------- acompanhamento (US-13) ----------
  private static readonly POLLING_MS = 10_000;
  private polling?: ReturnType<typeof setInterval>;

  aguardandoPagamento = computed(() => this.pedido()?.status === 'AGUARDANDO_PAGAMENTO');
  modo = signal<'cartao' | 'pix'>('cartao');

  // ---------- cartão (US-09) ----------
  pagando = signal(false);
  recusa = signal<string | null>(null);

  form = this.fb.nonNullable.group({
    forma: ['CARTAO_CREDITO' as FormaCartao, Validators.required],
    numero: ['', [Validators.required, numeroCartao]],
    nomeTitular: ['', [Validators.required, Validators.maxLength(60)]],
    validade: ['', [Validators.required, Validators.pattern(/^(0[1-9]|1[0-2])\/\d{2}$/), validadeCartao]],
    cvv: ['', [Validators.required, Validators.pattern(/^\d{3}$/)]],
  });

  // ---------- PIX (US-10) ----------
  pix = signal<PagamentoResponse | null>(null);
  gerandoPix = signal(false);
  confirmandoPix = signal(false);

  /** "Relógio" da tela: atualizado a cada segundo enquanto há um PIX na tela */
  private agora = signal(Date.now());
  private timer?: ReturnType<typeof setInterval>;

  segundosRestantes = computed(() => {
    const expiraEm = this.pix()?.pixExpiraEm;
    if (!expiraEm) return 0;
    return Math.max(0, Math.floor((new Date(expiraEm).getTime() - this.agora()) / 1000));
  });

  pixExpirado = computed(() => this.pix() !== null && this.segundosRestantes() === 0);

  /** 1785 → "29:45" */
  contagem = computed(() => {
    const s = this.segundosRestantes();
    return `${String(Math.floor(s / 60)).padStart(2, '0')}:${String(s % 60).padStart(2, '0')}`;
  });

  constructor() {
    addIcons({
      checkmarkCircleOutline, cardOutline, lockClosedOutline, qrCodeOutline, copyOutline, timeOutline,
      closeCircleOutline, ellipseOutline,
    });
    // Sem isso, os setInterval continuariam rodando depois de sair da tela (vazamento de memória)
    this.destroyRef.onDestroy(() => {
      this.pararRelogio();
      this.pararPolling();
    });
  }

  private get id(): number {
    return Number(this.route.snapshot.paramMap.get('id'));
  }

  ngOnInit() {
    this.pedidoService.buscar(this.id).subscribe({
      next: p => this.pedido.set(p),
      error: () => this.erro.set(true),
    });
  }

  /** Polling só enquanto a tela está visível (CA da US-13) */
  ionViewWillEnter() {
    this.iniciarPolling();
  }

  ionViewWillLeave() {
    this.pararPolling();
  }

  trocarModo(evento: CustomEvent) {
    this.modo.set(evento.detail.value);
  }

  // ---------- cartão ----------

  /** Máscara: "4111111111111111" → "4111 1111 1111 1111" */
  formatarNumero(evento: CustomEvent) {
    const digitos = String(evento.detail.value ?? '').replace(/\D/g, '').slice(0, 16);
    this.form.controls.numero.setValue(digitos.replace(/(\d{4})(?=\d)/g, '$1 '));
  }

  /** Máscara: "1228" → "12/28" */
  formatarValidade(evento: CustomEvent) {
    const digitos = String(evento.detail.value ?? '').replace(/\D/g, '').slice(0, 4);
    this.form.controls.validade.setValue(digitos.length > 2 ? `${digitos.slice(0, 2)}/${digitos.slice(2)}` : digitos);
  }

  erroValidade(): string {
    const erros = this.form.controls.validade.errors;
    if (erros?.['vencido']) return 'Cartão vencido.';
    if (erros?.['distante']) return 'Validade inválida.';
    return 'Use MM/AA.';
  }

  pagar() {
    const p = this.pedido();
    if (!p || this.pagando()) return;
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.pagando.set(true);
    this.recusa.set(null);

    this.pagamentoService.pagarComCartao(p.id, this.form.getRawValue()).subscribe({
      next: r => {
        this.pagando.set(false);
        if (r.status === 'APROVADO') {
          this.form.reset();   // CA#3: dados do cartão não ficam na tela
          this.marcarComoPago(`Pagamento aprovado no ${r.bandeira} final ${r.ultimosDigitos}! 🎉`);
        } else {
          // Recusa não é erro: mostra o motivo e deixa tentar outro cartão
          this.recusa.set(r.motivo ?? 'Pagamento recusado.');
          this.form.controls.cvv.reset();
        }
      },
      error: (err: HttpErrorResponse) => {
        this.pagando.set(false);
        this.avisar(err.error?.mensagem ?? 'Não foi possível processar o pagamento.', 'danger');
      },
    });
  }

  // ---------- PIX ----------

  gerarPix() {
    const p = this.pedido();
    if (!p || this.gerandoPix()) return;

    this.gerandoPix.set(true);
    this.pagamentoService.gerarPix(p.id).subscribe({
      next: r => {
        this.gerandoPix.set(false);
        this.pix.set(r);
        this.desenharQrCode(r.pixCopiaCola);
        this.iniciarRelogio();
      },
      error: (err: HttpErrorResponse) => {
        this.gerandoPix.set(false);
        this.avisar(err.error?.mensagem ?? 'Não foi possível gerar o PIX.', 'danger');
      },
    });
  }

  async copiarPix() {
    const codigo = this.pix()?.pixCopiaCola;
    if (!codigo) return;
    try {
      await navigator.clipboard.writeText(codigo);
      this.avisar('Código PIX copiado!', 'success');
    } catch {
      this.avisar('Não foi possível copiar. Selecione o código e copie manualmente.', 'danger');
    }
  }

  /** Desenha o "copia e cola" como imagem (data URL), sem chamar nenhum servidor */
  private async desenharQrCode(codigo: string | null) {
    this.qrCode.set(codigo ? await toDataURL(codigo, { width: 220, margin: 1 }) : null);
  }

  /** SIMULAÇÃO: na vida real, quem avisa a loja é o banco (webhook) */
  confirmarPix() {
    const p = this.pedido();
    if (!p || this.confirmandoPix()) return;

    this.confirmandoPix.set(true);
    this.pagamentoService.confirmarPix(p.id).subscribe({
      next: () => {
        this.confirmandoPix.set(false);
        this.marcarComoPago('Pagamento via PIX confirmado! 🎉');
      },
      error: (err: HttpErrorResponse) => {
        this.confirmandoPix.set(false);
        this.avisar(err.error?.mensagem ?? 'Não foi possível confirmar o PIX.', 'danger');
      },
    });
  }

  // ---------- acompanhamento (US-13) ----------

  private iniciarPolling() {
    this.pararPolling();
    this.polling = setInterval(() => this.atualizarStatus(), PedidoConfirmacaoPage.POLLING_MS);
  }

  private pararPolling() {
    if (this.polling) clearInterval(this.polling);
    this.polling = undefined;
  }

  private atualizarStatus() {
    const atual = this.pedido();
    if (!atual) return;
    if (STATUS_FINAIS.includes(atual.status)) {
      this.pararPolling();   // entregue/cancelado não muda mais
      return;
    }

    this.pedidoService.buscar(this.id).subscribe(novo => {
      if (novo.status === atual.status) return;
      this.pedido.set(novo);
      if (novo.status !== 'AGUARDANDO_PAGAMENTO') this.limparPix();
      this.avisar(
        `Seu pedido agora está: ${STATUS_LABEL[novo.status]}`,
        novo.status === 'CANCELADO' ? 'danger' : 'success',
      );
    });
  }

  etapaFeita(i: number): boolean {
    return i <= this.indiceAtual();
  }

  rotulo(status: StatusPedido): string {
    return STATUS_LABEL[status];
  }

  // ---------- comum ----------

  private marcarComoPago(mensagem: string) {
    const p = this.pedido();
    if (p) this.pedido.set({ ...p, status: 'PAGO' });
    this.limparPix();
    this.avisar(mensagem, 'success');
  }

  /** Some com o PIX da tela (pago, ou o pedido mudou por fora, ex.: admin cancelou) */
  private limparPix() {
    this.pix.set(null);
    this.qrCode.set(null);
    this.pararRelogio();
  }

  private iniciarRelogio() {
    this.pararRelogio();
    this.agora.set(Date.now());
    this.timer = setInterval(() => this.agora.set(Date.now()), 1000);
  }

  private pararRelogio() {
    if (this.timer) clearInterval(this.timer);
    this.timer = undefined;
  }

  private async avisar(mensagem: string, cor: 'success' | 'danger') {
    const t = await this.toast.create({ message: mensagem, duration: 4000, position: 'top', color: cor });
    await t.present();
  }
}