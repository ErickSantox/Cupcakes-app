export interface ItemPedidoRequest {
  produtoId: number;
  quantidade: number;
}

export interface PedidoRequest {
  enderecoId: number;
  itens: ItemPedidoRequest[];
}

export interface ItemPedidoResponse {
  produtoId: number;
  produtoNome: string;
  quantidade: number;
  precoUnitario: number;
  subtotal: number;
}

export type StatusPedido =
  | 'AGUARDANDO_PAGAMENTO' | 'PAGO' | 'EM_PREPARO' | 'SAIU_PARA_ENTREGA' | 'ENTREGUE' | 'CANCELADO';

/** Texto amigável de cada status (usado nas telas de pedido) */
export const STATUS_LABEL: Record<StatusPedido, string> = {
  AGUARDANDO_PAGAMENTO: 'Aguardando pagamento',
  PAGO: 'Pago',
  EM_PREPARO: 'Em preparo',
  SAIU_PARA_ENTREGA: 'Saiu para entrega',
  ENTREGUE: 'Entregue',
  CANCELADO: 'Cancelado',
};

export interface PedidoResponse {
  id: number;
  numero: string;
  status: StatusPedido;
  subtotal: number;
  frete: number;
  total: number;
  criadoEm: string;
  entregaLogradouro: string;
  entregaNumero: string;
  entregaComplemento: string | null;
  entregaBairro: string;
  entregaCidade: string;
  entregaUf: string;
  entregaCep: string;
  itens: ItemPedidoResponse[];
}

export interface FreteResponse {
  valor: number;
  prazoDias: number;
  gratis: boolean;
}

/** Formato do PagedModel do Spring */
export interface Pagina<T> {
  content: T[];
  page: { size: number; number: number; totalElements: number; totalPages: number };
}

/** Próximo passo do fluxo que o ADMIN pode dar (espelha StatusPedido.podeIrPara, sem o PAGO) */
export const PROXIMO_STATUS: Partial<Record<StatusPedido, StatusPedido>> = {
  PAGO: 'EM_PREPARO',
  EM_PREPARO: 'SAIU_PARA_ENTREGA',
  SAIU_PARA_ENTREGA: 'ENTREGUE',
};

export const PODE_CANCELAR: StatusPedido[] = ['AGUARDANDO_PAGAMENTO', 'PAGO', 'EM_PREPARO'];