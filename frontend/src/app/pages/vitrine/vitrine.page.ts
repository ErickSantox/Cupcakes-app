import { Component, OnInit, inject, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { IonContent, IonHeader, IonTitle, IonToolbar, IonChip, IonLabel, IonGrid, IonRow, IonCol, IonCard, IonCardContent, IonImg, IonBadge, IonSkeletonText, IonRefresher, IonRefresherContent, IonSearchbar } from '@ionic/angular';
import { ProdutoService } from '../../services/produto.service';
import { CategoriaResponse, ProdutoResponse } from '../../models/produto.model';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-vitrine',
  templateUrl: './vitrine.page.html',
  styleUrls: ['./vitrine.page.scss'],
  imports: [
    CurrencyPipe, RouterLink,
    IonContent, IonHeader, IonTitle, IonToolbar, IonChip, IonLabel, IonGrid, IonRow, IonCol,
    IonCard, IonCardContent, IonImg, IonBadge, IonSkeletonText, IonRefresher, IonRefresherContent,
    IonSearchbar
],
})
export class VitrinePage implements OnInit {
  private produtoService = inject(ProdutoService);

  produtos = signal<ProdutoResponse[]>([]);
  categorias = signal<CategoriaResponse[]>([]);
  categoriaSelecionada = signal<number | null>(null);
  carregando = signal(true);
  busca = signal('');
  private requisicao?: Subscription;

  /** 4 cards "fantasma" para o skeleton enquanto carrega */
  readonly esqueletos = [1, 2, 3, 4];



  selecionarCategoria(id: number | null) {
    this.categoriaSelecionada.set(id);
    this.carregarProdutos();
  }

  carregarProdutos(evento?: CustomEvent) {
    this.carregando.set(true);
    this.requisicao?.unsubscribe();
    this.requisicao = this.produtoService.listar(this.categoriaSelecionada(), this.busca()).subscribe({
      next: p => this.produtos.set(p),
      complete: () => {
        this.carregando.set(false);
        (evento?.target as HTMLIonRefresherElement | undefined)?.complete();
      },
      error: () => this.carregando.set(false),
    });
  }

  /** O seed grava "/img/produtos/x.jpg"; no app, as imagens ficam em "assets/img/produtos/x.jpg" */
  fotoDe(p: ProdutoResponse): string {
    return `assets${p.fotoUrl}`;
  }

  fotoPadrao(evento: Event) {
    const img = evento.target as HTMLIonImgElement;
    img.src = 'assets/img/logo.png';
    img.classList.add('sem-foto');
  }

  onBusca(evento: CustomEvent) {
    this.busca.set(evento.detail.value ?? '');
    this.carregarProdutos();
  }

    ngOnInit() {
    this.produtoService.categorias().subscribe(c => this.categorias.set(c));
    this.carregarProdutos();
  }
}