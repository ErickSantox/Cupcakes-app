import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Pagina, PedidoResponse, StatusPedido } from '../models/pedido.model';

export interface FiltroPedidos {
  status?: StatusPedido | '';
  de?: string;   // yyyy-MM-dd
  ate?: string;  // yyyy-MM-dd
  page?: number;
  size?: number;
}

@Injectable({ providedIn: 'root' })
export class AdminPedidoService {
  private http = inject(HttpClient);
  private url = `${environment.apiUrl}/admin/pedidos`;

  listar(filtro: FiltroPedidos): Observable<Pagina<PedidoResponse>> {
    let params = new HttpParams()
      .set('page', filtro.page ?? 0)
      .set('size', filtro.size ?? 10);
    if (filtro.status) params = params.set('status', filtro.status);
    if (filtro.de) params = params.set('de', filtro.de);
    if (filtro.ate) params = params.set('ate', filtro.ate);
    return this.http.get<Pagina<PedidoResponse>>(this.url, { params });
  }

  buscar(id: number): Observable<PedidoResponse> {
    return this.http.get<PedidoResponse>(`${this.url}/${id}`);
  }

  mudarStatus(id: number, status: StatusPedido): Observable<PedidoResponse> {
    return this.http.patch<PedidoResponse>(`${this.url}/${id}/status`, { status });
  }
}