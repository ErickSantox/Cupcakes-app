import { Injectable, computed, effect, signal } from '@angular/core';
import { ItemCarrinho } from '../models/carrinho.model';
import { ProdutoResponse } from '../models/produto.model';

const CHAVE = 'cupcakes.carrinho';
const VALIDADE_MS = 24 * 60 * 60 * 1000; // RNF#1: 24 horas

export const MAX_POR_ITEM = 50; // RN#1

@Injectable({ providedIn: 'root' })
export class CarrinhoService {
  private _itens = signal<ItemCarrinho[]>(this.carregar());

  /** Somente leitura para as páginas: só o service altera o carrinho */
  itens = this._itens.asReadonly();

  quantidadeTotal = computed(() => this._itens().reduce((soma, i) => soma + i.quantidade, 0));
  total = computed(() => this._itens().reduce((soma, i) => soma + i.preco * i.quantidade, 0));

  constructor() {
    // Salva no localStorage sempre que o carrinho mudar
    effect(() => {
      localStorage.setItem(CHAVE, JSON.stringify({ itens: this._itens(), salvoEm: Date.now() }));
    });
  }

  adicionar(produto: ProdutoResponse, quantidade = 1): boolean {
    const existente = this._itens().find(i => i.produtoId === produto.id);
    const desejada = (existente?.quantidade ?? 0) + quantidade;
    const final = Math.min(desejada, MAX_POR_ITEM);

    if (existente) {
      this._itens.update(lista =>
        lista.map(i => i.produtoId === produto.id ? { ...i, quantidade: final } : i));
    } else {
      this._itens.update(lista => [...lista, {
        produtoId: produto.id, nome: produto.nome, preco: produto.preco,
        fotoUrl: produto.fotoUrl, quantidade: final,
      }]);
    }
    return desejada > MAX_POR_ITEM;
  }

  alterarQuantidade(produtoId: number, quantidade: number) {
    if (quantidade <= 0) {
      this.remover(produtoId);
      return;
    }
    const final = Math.min(quantidade, MAX_POR_ITEM);
    this._itens.update(lista =>
      lista.map(i => i.produtoId === produtoId ? { ...i, quantidade: final } : i));
  }

  /** Retorna o item removido, para o "Desfazer" (US-07) */
  remover(produtoId: number): ItemCarrinho | undefined {
    const item = this._itens().find(i => i.produtoId === produtoId);
    this._itens.update(lista => lista.filter(i => i.produtoId !== produtoId));
    return item;
  }

  restaurar(item: ItemCarrinho) {
    this._itens.update(lista => [...lista, item]);
  }

  limpar() {
    this._itens.set([]);
  }

  private carregar(): ItemCarrinho[] {
    const bruto = localStorage.getItem(CHAVE);
    if (!bruto) return [];
    try {
      const salvo = JSON.parse(bruto);
      if (Date.now() - salvo.salvoEm > VALIDADE_MS) return [];
      return salvo.itens ?? [];
    } catch {
      return []; // JSON corrompido não pode derrubar o app
    }
  }
}