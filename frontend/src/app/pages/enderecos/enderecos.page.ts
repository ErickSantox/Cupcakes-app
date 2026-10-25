import { Component, OnInit, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import {
  IonContent, IonHeader, IonTitle, IonToolbar, IonButtons, IonBackButton, IonButton, IonIcon,
  IonCard, IonCardContent, IonBadge, IonModal, IonInput, IonCheckbox, IonNote, IonSpinner,
  AlertController, ToastController,
} from '@ionic/angular';
import { addIcons } from 'ionicons';
import { add, createOutline, trashOutline, starOutline, locationOutline } from 'ionicons/icons';
import { EnderecoService } from '../../services/endereco.service';
import { EnderecoResponse } from '../../models/endereco.model';

@Component({
  selector: 'app-enderecos',
  templateUrl: './enderecos.page.html',
  styleUrls: ['./enderecos.page.scss'],
  imports: [
    ReactiveFormsModule,
    IonContent, IonHeader, IonTitle, IonToolbar, IonButtons, IonBackButton, IonButton, IonIcon,
    IonCard, IonCardContent, IonBadge, IonModal, IonInput, IonCheckbox, IonNote, IonSpinner,
  ],
})
export class EnderecosPage implements OnInit {
  private enderecoService = inject(EnderecoService);
  private alert = inject(AlertController);
  private toast = inject(ToastController);
  private fb = inject(FormBuilder);

  readonly MAX = 5;   // RN#1

  enderecos = signal<EnderecoResponse[]>([]);
  carregando = signal(true);
  modalAberto = signal(false);
  editandoId = signal<number | null>(null);
  buscandoCep = signal(false);
  avisoCep = signal('');
  salvando = signal(false);

  form = this.fb.nonNullable.group({
    cep: ['', [Validators.required, Validators.pattern(/^\d{5}-?\d{3}$/)]],   // CA#3
    logradouro: ['', [Validators.required, Validators.maxLength(100)]],
    numero: ['', [Validators.required, Validators.maxLength(10)]],
    complemento: ['', Validators.maxLength(50)],
    bairro: ['', [Validators.required, Validators.maxLength(50)]],
    cidade: ['', [Validators.required, Validators.maxLength(50)]],
    uf: ['', [Validators.required, Validators.pattern(/^[A-Za-z]{2}$/)]],
    padrao: [false],
  });

  constructor() {
    addIcons({ add, createOutline, trashOutline, starOutline, locationOutline });
  }

  ngOnInit() {
    this.carregar();
  }

  carregar() {
    this.carregando.set(true);
    this.enderecoService.listar().subscribe({
      next: e => this.enderecos.set(e),
      complete: () => this.carregando.set(false),
      error: () => this.carregando.set(false),
    });
  }

  novo() {
    this.editandoId.set(null);
    this.form.reset();
    this.avisoCep.set('');
    this.modalAberto.set(true);
  }

  editar(e: EnderecoResponse) {
    this.editandoId.set(e.id);
    this.form.setValue({
      cep: e.cep, logradouro: e.logradouro, numero: e.numero, complemento: e.complemento ?? '',
      bairro: e.bairro, cidade: e.cidade, uf: e.uf, padrao: e.padrao,
    });
    this.avisoCep.set('');
    this.modalAberto.set(true);
  }

  /** RNF#1: quando o CEP tiver 8 dígitos, busca no ViaCEP e preenche o resto */
  onCep(evento: CustomEvent) {
    const cep = (evento.detail.value ?? '').replace(/\D/g, '');
    this.avisoCep.set('');
    if (cep.length !== 8) return;

    this.buscandoCep.set(true);
    this.enderecoService.buscarCep(cep).subscribe({
      next: r => {
        this.buscandoCep.set(false);
        if (r.erro) {
          this.avisoCep.set('CEP não encontrado. Confira o número ou preencha o endereço manualmente.');
          return;
        }
        this.form.patchValue({ logradouro: r.logradouro, bairro: r.bairro, cidade: r.localidade, uf: r.uf });
      },
      // ViaCEP fora do ar não pode travar o cadastro
      error: () => {
        this.buscandoCep.set(false);
        this.avisoCep.set('Não foi possível buscar o CEP agora. Preencha o endereço manualmente.');
      },
    });
  }

  salvar() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const valor = this.form.getRawValue();
    const req = { ...valor, cep: valor.cep.replace(/\D/g, ''), uf: valor.uf.toUpperCase() };
    const id = this.editandoId();
    const chamada = id ? this.enderecoService.atualizar(id, req) : this.enderecoService.criar(req);

    this.salvando.set(true);
    chamada.subscribe({
      next: () => {
        this.salvando.set(false);
        this.modalAberto.set(false);
        this.carregar();
        this.mostrar(id ? 'Endereço atualizado.' : 'Endereço cadastrado! 📍');
      },
      error: (e) => {
        this.salvando.set(false);
        const campos = e.error?.campos;
        if (campos) this.mostrar(Object.values(campos)[0] as string, 'danger');
      },
    });
  }

  definirPadrao(e: EnderecoResponse) {
    this.enderecoService.definirPadrao(e.id).subscribe(() => {
      this.carregar();
      this.mostrar('Endereço padrão atualizado.');
    });
  }

  /** Excluir é irreversível: pede confirmação antes */
  async excluir(e: EnderecoResponse) {
    const a = await this.alert.create({
      header: 'Excluir endereço?',
      message: `${e.logradouro}, ${e.numero} será removido.`,
      buttons: [
        { text: 'Cancelar', role: 'cancel' },
        {
          text: 'Excluir', role: 'destructive',
          handler: () => this.enderecoService.excluir(e.id).subscribe(() => {
            this.carregar();
            this.mostrar('Endereço excluído.');
          }),
        },
      ],
    });
    await a.present();
  }

  formatarCep(cep: string): string {
    return `${cep.slice(0, 5)}-${cep.slice(5)}`;
  }

  private async mostrar(message: string, color = 'success') {
    const t = await this.toast.create({ message, color, duration: 2500, position: 'top' });
    await t.present();
  }
}