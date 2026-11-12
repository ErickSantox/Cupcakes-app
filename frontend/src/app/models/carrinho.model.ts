export interface ItemCarrinho {
  produtoId: number;
  nome: string;
  preco: number;
  fotoUrl: string;
  quantidade: number;
  /** Estoque no momento em que foi adicionado (B3). Opcional: carrinhos antigos no navegador não têm. */
  estoque?: number;
}