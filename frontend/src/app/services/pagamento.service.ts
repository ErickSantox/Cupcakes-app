import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { PagamentoCartaoRequest, PagamentoResponse } from '../models/pagamento.model';

@Injectable({ providedIn: 'root' })
export class PagamentoService {
  private http = inject(HttpClient);

  pagarComCartao(pedidoId: number, req: PagamentoCartaoRequest): Observable<PagamentoResponse> {
    return this.http.post<PagamentoResponse>(`${environment.apiUrl}/pedidos/${pedidoId}/pagamento/cartao`, req);
  }
}