import { Component, inject } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import {
  IonContent, IonHeader, IonTitle, IonToolbar, IonList, IonItem, IonThumbnail, IonImg, IonLabel,
  IonButton, IonIcon, IonFooter, ToastController,
} from '@ionic/angular';
import { addIcons } from 'ionicons';
import { add, remove, trashOutline, cartOutline } from 'ionicons/icons';
import { CarrinhoService, MAX_POR_ITEM } from '../../services/carrinho.service';
import { ItemCarrinho } from '../../models/carrinho.model';
import { urlDaFoto } from '../../utils/foto';
import { ProdutoResponse } from '../../models/produto.model';

@Component({
  selector: 'app-carrinho',
  templateUrl: './carrinho.page.html',
  styleUrls: ['./carrinho.page.scss'],
  imports: [
    CurrencyPipe, RouterLink,
    IonContent, IonHeader, IonTitle, IonToolbar, IonList, IonItem, IonThumbnail, IonImg, IonLabel,
    IonButton, IonIcon, IonFooter,
  ],
})
export class CarrinhoPage {
  carrinho = inject(CarrinhoService);
  private toast = inject(ToastController);

  readonly maximo = MAX_POR_ITEM;

  constructor() {
    addIcons({ add, remove, trashOutline, cartOutline });
  }

  mudarQuantidade(item: ItemCarrinho, delta: number) {
    this.carrinho.alterarQuantidade(item.produtoId, item.quantidade + delta);
  }

  /** Ação reversível (IHC): remove e oferece "Desfazer" */
  async remover(item: ItemCarrinho) {
    const removido = this.carrinho.remover(item.produtoId);
    if (!removido) return;

    const t = await this.toast.create({
      message: `${removido.nome} removido do carrinho.`,
      duration: 4000,
      position: 'top',
      buttons: [{ text: 'Desfazer', handler: () => this.carrinho.restaurar(removido) }],
    });
    await t.present();
  }

    fotoDe(item: ItemCarrinho): string {
    return urlDaFoto(item.fotoUrl);
  }

  fotoPadrao(evento: Event) {
    (evento.target as HTMLIonImgElement).src = 'assets/img/logo.png';
  }
}