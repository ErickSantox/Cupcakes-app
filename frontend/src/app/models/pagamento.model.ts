export type FormaCartao = 'CARTAO_CREDITO' | 'CARTAO_DEBITO';
export type StatusPagamento = 'PENDENTE' | 'APROVADO' | 'RECUSADO' | 'EXPIRADO';

export interface PagamentoCartaoRequest {
  numero: string;
  nomeTitular: string;
  validade: string;
  cvv: string;
  forma: FormaCartao;
}

export interface PagamentoResponse {
  id: number;
  forma: string;
  status: StatusPagamento;
  valor: number;
  bandeira: string | null;
  ultimosDigitos: string | null;
  motivo: string | null;
  criadoEm: string;
  pixCopiaCola: string | null;
  pixExpiraEm: string | null;
}