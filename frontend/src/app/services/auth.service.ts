import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs';
import { CadastroRequest, UsuarioResponse } from '../models/usuario.model';

@Injectable({ providedIn: 'root' })
export class AuthService {
    private http = inject(HttpClient);

    cadastrar(dados: CadastroRequest): Observable<UsuarioResponse> {
        return this.http.post<UsuarioResponse>(`${environment.apiUrl}/auth/cadastro`, dados);
    }
}