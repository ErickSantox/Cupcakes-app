import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import {
  IonContent, IonHeader, IonTitle, IonToolbar, IonList, IonItem, IonLabel, IonNote,
  IonButton, IonIcon, IonBadge, IonSpinner, IonInput, IonSegment, IonSegmentButton,
  ToastController,
} from '@ionic/angular';
import { addIcons } from 'ionicons';
import { checkmarkCircleOutline, cardOutline, lockClosedOutline } from 'ionicons/icons';
import { PedidoService } from '../../services/pedido.service';
import { PagamentoService } from '../../services/pagamento.service';
import { PedidoResponse, STATUS_LABEL } from '../../models/pedido.model';
import { FormaCartao } from '../../models/pagamento.model';

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

@Component({
  selector: 'app-pedido-confirmacao',
  templateUrl: './pedido-confirmacao.page.html',
  styleUrls: ['./pedido-confirmacao.page.scss'],
  imports: [
    CurrencyPipe, RouterLink, ReactiveFormsModule,
    IonContent, IonHeader, IonTitle, IonToolbar, IonList, IonItem, IonLabel, IonNote,
    IonButton, IonIcon, IonBadge, IonSpinner, IonInput, IonSegment, IonSegmentButton,
  ],
})
export class PedidoConfirmacaoPage implements OnInit {
  private route = inject(ActivatedRoute);
  private pedidoService = inject(PedidoService);
  private pagamentoService = inject(PagamentoService);
  private toast = inject(ToastController);
  private fb = inject(FormBuilder);

  readonly statusLabel = STATUS_LABEL;
  pedido = signal<PedidoResponse | null>(null);
  erro = signal(false);

  pagando = signal(false);
  recusa = signal<string | null>(null);

  aguardandoPagamento = computed(() => this.pedido()?.status === 'AGUARDANDO_PAGAMENTO');

  form = this.fb.nonNullable.group({
    forma: ['CARTAO_CREDITO' as FormaCartao, Validators.required],
    numero: ['', [Validators.required, Validators.pattern(/^[\d ]{13,23}$/)]],
    nomeTitular: ['', [Validators.required, Validators.maxLength(60)]],
    validade: ['', [Validators.required, Validators.pattern(/^(0[1-9]|1[0-2])\/\d{2}$/), validadeCartao]],
    cvv: ['', [Validators.required, Validators.pattern(/^\d{3}$/)]],
  });

  constructor() {
    addIcons({ checkmarkCircleOutline, cardOutline, lockClosedOutline });
  }

  ngOnInit() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.pedidoService.buscar(id).subscribe({
      next: p => this.pedido.set(p),
      error: () => this.erro.set(true),
    });
  }

  /** Máscara: "4111111111111111" → "4111 1111 1111 1111" */
  formatarNumero(evento: CustomEvent) {
    const digitos = String(evento.detail.value ?? '').replace(/\D/g, '').slice(0, 19);
    this.form.controls.numero.setValue(digitos.replace(/(\d{4})(?=\d)/g, '$1 '));
  }

  /** Máscara: "1228" → "12/28" */
  formatarValidade(evento: CustomEvent) {
    const digitos = String(evento.detail.value ?? '').replace(/\D/g, '').slice(0, 4);
    this.form.controls.validade.setValue(digitos.length > 2 ? `${digitos.slice(0, 2)}/${digitos.slice(2)}` : digitos);
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
          this.pedido.set({ ...p, status: 'PAGO' });
          this.avisar(`Pagamento aprovado no ${r.bandeira} final ${r.ultimosDigitos}! 🎉`, 'success');
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

  private async avisar(mensagem: string, cor: 'success' | 'danger') {
    const t = await this.toast.create({ message: mensagem, duration: 4000, position: 'top', color: cor });
    await t.present();
  }
  
  erroValidade(): string {
  const erros = this.form.controls.validade.errors;
  if (erros?.['vencido']) return 'Cartão vencido.';
  if (erros?.['distante']) return 'Validade inválida.';
  return 'Use MM/AA.';
}
}