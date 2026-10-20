import { Component, OnInit, inject, input, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import {
  IonContent, IonHeader, IonTitle, IonToolbar, IonButtons, IonBackButton, IonImg, IonBadge, IonSpinner,
} from '@ionic/angular';
import { ProdutoService } from '../../services/produto.service';
import { ProdutoResponse } from '../../models/produto.model';

@Component({
  selector: 'app-produto-detalhe',
  templateUrl: './produto-detalhe.page.html',
  styleUrls: ['./produto-detalhe.page.scss'],
  imports: [
    CurrencyPipe,
    IonContent, IonHeader, IonTitle, IonToolbar, IonButtons, IonBackButton, IonImg, IonBadge, IonSpinner,
  ],
})
export class ProdutoDetalhePage implements OnInit {
  private produtoService = inject(ProdutoService);

  /** Vem direto da URL /produto/:id (graças ao withComponentInputBinding no main.ts) */
  id = input.required<string>();

  produto = signal<ProdutoResponse | null>(null);

  ngOnInit() {
    this.produtoService.buscar(Number(this.id())).subscribe(p => this.produto.set(p));
  }

  fotoPadrao(evento: Event) {
    const img = evento.target as HTMLIonImgElement;
    img.src = 'assets/img/logo.png';
    img.classList.add('sem-foto');
  }
}