import { Component, OnInit, inject, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import {
  IonContent, IonHeader, IonTitle, IonToolbar, IonList, IonItem, IonLabel, IonNote,
  IonButton, IonIcon, IonBadge, IonSpinner,
} from '@ionic/angular';
import { addIcons } from 'ionicons';
import { checkmarkCircleOutline } from 'ionicons/icons';
import { PedidoService } from '../../services/pedido.service';
import { PedidoResponse, STATUS_LABEL } from '../../models/pedido.model';

@Component({
  selector: 'app-pedido-confirmacao',
  templateUrl: './pedido-confirmacao.page.html',
  styleUrls: ['./pedido-confirmacao.page.scss'],
  imports: [
    CurrencyPipe, RouterLink,
    IonContent, IonHeader, IonTitle, IonToolbar, IonList, IonItem, IonLabel, IonNote,
    IonButton, IonIcon, IonBadge, IonSpinner,
  ],
})
export class PedidoConfirmacaoPage implements OnInit {
  private route = inject(ActivatedRoute);
  private pedidoService = inject(PedidoService);

  readonly statusLabel = STATUS_LABEL;
  pedido = signal<PedidoResponse | null>(null);
  erro = signal(false);

  constructor() {
    addIcons({ checkmarkCircleOutline });
  }

  ngOnInit() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.pedidoService.buscar(id).subscribe({
      next: p => this.pedido.set(p),
      error: () => this.erro.set(true),
    });
  }
}