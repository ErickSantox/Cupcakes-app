import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import {
  IonContent, IonHeader, IonTitle, IonToolbar, IonButtons, IonBackButton, IonImg, IonBadge, IonSpinner,
  IonFooter, IonButton, IonIcon, ToastController,
} from '@ionic/angular';
import { ProdutoService } from '../../services/produto.service';
import { ProdutoResponse } from '../../models/produto.model';
import { Router } from '@angular/router';
import { addIcons } from 'ionicons';
import { add, remove, cartOutline } from 'ionicons/icons';
import { CarrinhoService, MAX_POR_ITEM } from '../../services/carrinho.service';
import { urlDaFoto } from '../../utils/foto';

@Component({
  selector: 'app-produto-detalhe',
  templateUrl: './produto-detalhe.page.html',
  styleUrls: ['./produto-detalhe.page.scss'],
  imports: [
    CurrencyPipe,
    IonContent, IonHeader, IonTitle, IonToolbar, IonButtons, IonBackButton, IonImg, IonBadge, IonSpinner, IonFooter, IonButton, IonIcon
  ],
})
export class ProdutoDetalhePage implements OnInit {
  private produtoService = inject(ProdutoService);

  id = input.required<string>();

  produto = signal<ProdutoResponse | null>(null);

  ngOnInit() {
    this.produtoService.buscar(Number(this.id())).subscribe(p => this.produto.set(p));
  }

  private carrinho = inject(CarrinhoService);
  private toast = inject(ToastController);
  private router = inject(Router);

  quantidade = signal(1);

  /** Não deixa escolher mais do que o estoque nem mais que 50 (RN#1) */
  maximo = computed(() => Math.min(this.produto()?.estoque ?? 1, MAX_POR_ITEM));

  constructor() {
    addIcons({ add, remove, cartOutline });
  }

  mudarQuantidade(delta: number) {
    const nova = this.quantidade() + delta;
    if (nova >= 1 && nova <= this.maximo()) {
      this.quantidade.set(nova);
    }
  }

  async adicionar() {
    const p = this.produto();
    if (!p) return;

    const limitou = this.carrinho.adicionar(p, this.quantidade());

    const t = await this.toast.create({
      message: limitou
        ? `Limite de ${MAX_POR_ITEM} unidades por sabor. Seu carrinho ficou com o máximo.`
        : `${this.quantidade()}x ${p.nome} no carrinho! 🧁`,
      color: limitou ? 'warning' : 'success',
      duration: 2500,
      position: 'top',
      buttons: [{ text: 'Ver carrinho', handler: () => this.router.navigateByUrl('/tabs/carrinho') }],
    });
    await t.present();
    this.quantidade.set(1);
  }

    fotoDe(p: ProdutoResponse): string {
    return urlDaFoto(p.fotoUrl);
  }

  fotoPadrao(evento: Event) {
    const img = evento.target as HTMLIonImgElement;
    img.src = 'assets/img/logo.png';
    img.classList.add('sem-foto');
  }
}