import { Component, inject, signal } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink } from '@angular/router';
import { filter } from 'rxjs';
import {
  IonContent, IonHeader, IonTitle, IonToolbar, IonList, IonItem, IonLabel, IonBadge,
  IonButton, IonRefresher, IonRefresherContent, IonSpinner,
} from '@ionic/angular';
import { PedidoService } from '../../services/pedido.service';
import { SessaoService } from '../../services/sessao.service';
import { COR_STATUS, PedidoResponse, STATUS_LABEL } from '../../models/pedido.model';

@Component({
  selector: 'app-pedidos',
  templateUrl: './pedidos.page.html',
  styleUrls: ['./pedidos.page.scss'],
  imports: [
    CurrencyPipe, DatePipe, RouterLink,
    IonContent, IonHeader, IonTitle, IonToolbar, IonList, IonItem, IonLabel, IonBadge,
    IonButton, IonRefresher, IonRefresherContent, IonSpinner,
  ],
})
export class PedidosPage {
  private pedidoService = inject(PedidoService);
  private router = inject(Router);
  readonly sessao = inject(SessaoService);

  readonly STATUS_LABEL = STATUS_LABEL;
  readonly COR_STATUS = COR_STATUS;

  pedidos = signal<PedidoResponse[]>([]);
  carregando = signal(false);

  constructor() {
    // Recarrega sempre que a aba volta a ser a rota ativa: clique na aba, ion-back-button,
    // botão "voltar" do navegador/mouse ou F5. O ionViewWillEnter não pega o voltar do
    // navegador quando a página está dentro das tabs.
    this.router.events.pipe(
      filter((e): e is NavigationEnd => e instanceof NavigationEnd),
      filter(e => e.urlAfterRedirects.startsWith('/tabs/pedidos')),
      takeUntilDestroyed(),
    ).subscribe(() => this.carregar());
  }

  carregar(evento?: CustomEvent) {
    if (!this.sessao.logado()) {
      this.pedidos.set([]);
      (evento?.target as HTMLIonRefresherElement | undefined)?.complete();
      return;
    }
    // Spinner só na primeira carga; nas próximas a lista antiga fica na tela até chegar a nova
    this.carregando.set(!evento && this.pedidos().length === 0);
    this.pedidoService.meus().subscribe({
      next: lista => this.pedidos.set(lista),
      complete: () => this.finalizar(evento),
      error: () => this.finalizar(evento),
    });
  }

  private finalizar(evento?: CustomEvent) {
    this.carregando.set(false);
    (evento?.target as HTMLIonRefresherElement | undefined)?.complete();
  }
}