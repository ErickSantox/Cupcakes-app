import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ProdutoRequest, ProdutoResponse } from '../models/produto.model';

@Injectable({ providedIn: 'root' })
export class AdminProdutoService {
  private http = inject(HttpClient);
  private url = `${environment.apiUrl}/admin/produtos`;

  listar(): Observable<ProdutoResponse[]> {
    return this.http.get<ProdutoResponse[]>(this.url);
  }

  criar(req: ProdutoRequest): Observable<ProdutoResponse> {
    return this.http.post<ProdutoResponse>(this.url, req);
  }

  atualizar(id: number, req: ProdutoRequest): Observable<ProdutoResponse> {
    return this.http.put<ProdutoResponse>(`${this.url}/${id}`, req);
  }

  alterarAtivo(id: number, ativo: boolean): Observable<ProdutoResponse> {
    return this.http.patch<ProdutoResponse>(`${this.url}/${id}/ativo`, { ativo });
  }
}