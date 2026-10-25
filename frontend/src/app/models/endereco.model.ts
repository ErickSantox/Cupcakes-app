export interface EnderecoRequest {
  cep: string;
  logradouro: string;
  numero: string;
  complemento: string;
  bairro: string;
  cidade: string;
  uf: string;
  padrao: boolean;
}

export interface EnderecoResponse {
  id: number;
  cep: string;
  logradouro: string;
  numero: string;
  complemento: string | null;
  bairro: string;
  cidade: string;
  uf: string;
  padrao: boolean;
}

/** Formato que o ViaCEP devolve (só os campos que usamos) */
export interface ViaCepResponse {
  logradouro: string;
  bairro: string;
  localidade: string;   // a cidade, com outro nome no ViaCEP
  uf: string;
  erro?: boolean;       // vem true quando o CEP não existe
}