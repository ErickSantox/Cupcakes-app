import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { FreteResponse, PedidoRequest, PedidoResponse } from '../models/pedido.model';

@Injectable({ providedIn: 'root' })
export class PedidoService {
  private http = inject(HttpClient);
  private url = `${environment.apiUrl}/pedidos`;

  criar(req: PedidoRequest): Observable<PedidoResponse> {
    return this.http.post<PedidoResponse>(this.url, req);
  }

  buscar(id: number): Observable<PedidoResponse> {
    return this.http.get<PedidoResponse>(`${this.url}/${id}`);
  }

  /** US-12: só para exibir no checkout — o back recalcula ao criar o pedido */
  calcularFrete(cep: string, subtotal: number): Observable<FreteResponse> {
    const params = new HttpParams().set('cep', cep).set('subtotal', subtotal);
    return this.http.get<FreteResponse>(`${environment.apiUrl}/frete`, { params });
  }
}