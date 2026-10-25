import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { EnderecoRequest, EnderecoResponse, ViaCepResponse } from '../models/endereco.model';

@Injectable({ providedIn: 'root' })
export class EnderecoService {
  private http = inject(HttpClient);
  private url = `${environment.apiUrl}/enderecos`;

  listar(): Observable<EnderecoResponse[]> {
    return this.http.get<EnderecoResponse[]>(this.url);
  }

  criar(req: EnderecoRequest): Observable<EnderecoResponse> {
    return this.http.post<EnderecoResponse>(this.url, req);
  }

  atualizar(id: number, req: EnderecoRequest): Observable<EnderecoResponse> {
    return this.http.put<EnderecoResponse>(`${this.url}/${id}`, req);
  }

  definirPadrao(id: number): Observable<EnderecoResponse> {
    return this.http.patch<EnderecoResponse>(`${this.url}/${id}/padrao`, {});
  }

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }

  /** RNF#1: autopreenchimento pelo CEP (API pública, sem token) */
  buscarCep(cep: string): Observable<ViaCepResponse> {
    return this.http.get<ViaCepResponse>(`https://viacep.com.br/ws/${cep}/json/`);
  }
}