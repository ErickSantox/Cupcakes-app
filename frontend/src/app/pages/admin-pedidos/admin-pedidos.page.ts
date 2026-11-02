import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { ReactiveFormsModule, FormBuilder } from '@angular/forms';
import { toSignal } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import {
  IonContent, IonHeader, IonTitle, IonToolbar, IonButtons, IonBackButton, IonButton,
  IonSelect, IonSelectOption, IonInput, IonCard, IonCardHeader, IonCardTitle, IonCardSubtitle,
  IonCardContent, IonBadge, IonNote, AlertController, ToastController,
} from '@ionic/angular';
import { AdminPedidoService } from '../../services/admin-pedido.service';
import {
  PedidoResponse, StatusPedido, STATUS_LABEL, PROXIMO_STATUS, PODE_CANCELAR,
} from '../../models/pedido.model';

@Component({
  selector: 'app-admin-pedidos',
  templateUrl: './admin-pedidos.page.html',
  styleUrls: ['./admin-pedidos.page.scss'],
  imports: [
    CurrencyPipe, DatePipe, ReactiveFormsModule, RouterLink,
    IonContent, IonHeader, IonTitle, IonToolbar, IonButtons, IonBackButton, IonButton,
    IonSelect, IonSelectOption, IonInput, IonCard, IonCardHeader, IonCardTitle, IonCardSubtitle,
    IonCardContent, IonBadge, IonNote,
  ],
})
export class AdminPedidosPage implements OnInit {
  private service = inject(AdminPedidoService);
  private alertCtrl = inject(AlertController);
  private toast = inject(ToastController);
  private fb = inject(FormBuilder);

  readonly STATUS_LABEL = STATUS_LABEL;
  readonly statusLista = Object.keys(STATUS_LABEL) as StatusPedido[];

  pedidos = signal<PedidoResponse[]>([]);
  paginaAtual = signal(0);
  totalPaginas = signal(0);
  ocupado = signal<number | null>(null);   // id do pedido sendo alterado

  filtros = this.fb.nonNullable.group({
    status: '' as StatusPedido | '',
    de: '',
    ate: '',
  });

  private valorFiltros = toSignal(this.filtros.valueChanges, { initialValue: this.filtros.getRawValue() });

  /** yyyy-MM-dd compara certo como texto */
  datasInvalidas = computed(() => {
    const { de, ate } = this.valorFiltros();
    return !!de && !!ate && de > ate;
  });

  ngOnInit() {
    this.buscar(0);
  }

  buscar(pagina: number) {
    if (this.datasInvalidas()) return;
    this.service.listar({ ...this.filtros.getRawValue(), page: pagina }).subscribe(res => {
      this.pedidos.set(res.content);
      this.paginaAtual.set(res.page.number);
      this.totalPaginas.set(res.page.totalPages);
    });
  }

  limpar() {
    this.filtros.reset();
    this.buscar(0);
  }

  proximo(status: StatusPedido): StatusPedido | undefined {
    return PROXIMO_STATUS[status];
  }

  podeCancelar(status: StatusPedido): boolean {
    return PODE_CANCELAR.includes(status);
  }

  corDoStatus(status: StatusPedido): string {
    switch (status) {
      case 'AGUARDANDO_PAGAMENTO': return 'warning';
      case 'CANCELADO': return 'danger';
      case 'ENTREGUE': return 'success';
      default: return 'primary';
    }
  }

  async confirmarCancelamento(p: PedidoResponse) {
    const alert = await this.alertCtrl.create({
      header: 'Cancelar pedido?',
      message: `O pedido ${p.numero} será cancelado, o estoque devolvido e o pagamento estornado.`,
      buttons: [
        { text: 'Voltar', role: 'cancel' },
        { text: 'Cancelar pedido', role: 'destructive', handler: () => this.mudar(p, 'CANCELADO') },
      ],
    });
    await alert.present();
  }

  mudar(p: PedidoResponse, status: StatusPedido) {
    this.ocupado.set(p.id);
    this.service.mudarStatus(p.id, status).subscribe({
      next: (atualizado) => {
        this.pedidos.update(lista => lista.map(x => (x.id === atualizado.id ? atualizado : x)));
        this.ocupado.set(null);
        this.avisar(`Pedido ${atualizado.numero}: ${STATUS_LABEL[atualizado.status]}.`);
      },
      error: () => this.ocupado.set(null),   // o toast de erro já vem do erroInterceptor
    });
  }

  private async avisar(message: string) {
    const t = await this.toast.create({ message, color: 'success', duration: 2500, position: 'bottom' });
    await t.present();
  }
}