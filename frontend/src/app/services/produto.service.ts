import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { CategoriaResponse, ProdutoResponse } from '../models/produto.model';

@Injectable({ providedIn: 'root' })
export class ProdutoService {
  private http = inject(HttpClient);


  listar(categoriaId?: number | null, busca?: string): Observable<ProdutoResponse[]> {
    let params = new HttpParams();
    if (categoriaId) {
      params = params.set('categoriaId', categoriaId);
    }
    if (busca?.trim()) {
      params = params.set('busca', busca.trim());
    }
    return this.http.get<ProdutoResponse[]>(`${environment.apiUrl}/produtos`, { params });
  }

  buscar(id: number): Observable<ProdutoResponse> {
    return this.http.get<ProdutoResponse>(`${environment.apiUrl}/produtos/${id}`);
  }

  categorias(): Observable<CategoriaResponse[]> {
    return this.http.get<CategoriaResponse[]>(`${environment.apiUrl}/categorias`);
  }

}