import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Router, RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';
import {
  IonContent, IonHeader, IonTitle, IonToolbar, IonButtons, IonBackButton, IonList, IonListHeader,
  IonItem, IonLabel, IonRadioGroup, IonRadio, IonButton, IonFooter, IonSpinner, IonNote,
  ToastController,
} from '@ionic/angular';
import { CarrinhoService } from '../../services/carrinho.service';
import { EnderecoService } from '../../services/endereco.service';
import { PedidoService } from '../../services/pedido.service';
import { EnderecoResponse } from '../../models/endereco.model';
import { FreteResponse, PedidoRequest } from '../../models/pedido.model';

@Component({
  selector: 'app-checkout',
  templateUrl: './checkout.page.html',
  styleUrls: ['./checkout.page.scss'],
  imports: [
    CurrencyPipe, RouterLink,
    IonContent, IonHeader, IonTitle, IonToolbar, IonButtons, IonBackButton, IonList, IonListHeader,
    IonItem, IonLabel, IonRadioGroup, IonRadio, IonButton, IonFooter, IonSpinner, IonNote,
  ],
})
export class CheckoutPage implements OnInit {
  carrinho = inject(CarrinhoService);
  private enderecoService = inject(EnderecoService);
  private pedidoService = inject(PedidoService);
  private router = inject(Router);
  private toast = inject(ToastController);

  enderecos = signal<EnderecoResponse[]>([]);
  enderecoId = signal<number | null>(null);
  frete = signal<FreteResponse | null>(null);
  carregando = signal(true);
  enviando = signal(false);

  /** Prévia: o valor oficial é o que o back devolver */
  total = computed(() => this.carrinho.total() + (this.frete()?.valor ?? 0));

  private requisicaoFrete?: Subscription;

  ngOnInit() {
    if (this.carrinho.itens().length === 0) {
      this.router.navigateByUrl('/tabs/carrinho', { replaceUrl: true });
      return;
    }

    this.enderecoService.listar().subscribe({
      next: lista => {
        this.enderecos.set(lista);
        const inicial = lista.find(e => e.padrao) ?? lista[0];
        if (inicial) this.selecionar(inicial.id);
        this.carregando.set(false);
      },
      error: () => {
        this.carregando.set(false);
        this.avisar('Não foi possível carregar seus endereços.');
      },
    });
  }

  selecionar(id: number) {
    const endereco = this.enderecos().find(e => e.id === id);
    if (!endereco) return;

    this.enderecoId.set(id);
    this.frete.set(null);

    // Mesma ideia da busca na vitrine: cancela o cálculo anterior se o usuário trocar rápido
    this.requisicaoFrete?.unsubscribe();
    this.requisicaoFrete = this.pedidoService.calcularFrete(endereco.cep, this.carrinho.total()).subscribe({
      next: f => this.frete.set(f),
      error: () => this.avisar('Não foi possível calcular o frete.'),
    });
  }

  confirmar() {
    const enderecoId = this.enderecoId();
    if (!enderecoId || this.enviando()) return;   // evita pedido duplicado com duplo clique

    this.enviando.set(true);
    const req: PedidoRequest = {
      enderecoId,
      itens: this.carrinho.itens().map(i => ({ produtoId: i.produtoId, quantidade: i.quantidade })),
    };

    this.pedidoService.criar(req).subscribe({
      next: pedido => {
        this.carrinho.limpar();
        this.router.navigate(['/pedido', pedido.id], { replaceUrl: true });
      },
      error: (err: HttpErrorResponse) => {
        this.enviando.set(false);
        // 422 do back: "Estoque insuficiente para X.", "Produto indisponível." etc.
        this.avisar(err.error?.mensagem ?? 'Não foi possível finalizar o pedido.');
      },
    });
  }

  private async avisar(mensagem: string) {
    const t = await this.toast.create({ message: mensagem, duration: 4000, position: 'top', color: 'danger' });
    await t.present();
  }
}