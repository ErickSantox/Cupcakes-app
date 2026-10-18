import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { Observable, tap } from 'rxjs';
import { CadastroRequest, UsuarioResponse,LoginRequest,LoginResponse} from '../models/usuario.model';
import { SessaoService } from './sessao.service';

@Injectable({ providedIn: 'root' })
export class AuthService {
    private http = inject(HttpClient);
    private sessao = inject(SessaoService);


    cadastrar(dados: CadastroRequest): Observable<UsuarioResponse> {
        return this.http.post<UsuarioResponse>(`${environment.apiUrl}/auth/cadastro`, dados);
    }


  login(dados: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${environment.apiUrl}/auth/login`, dados).pipe(tap(resposta => this.sessao.salvar(resposta)));
  }

  logout(): void {
    this.sessao.limpar();
  }
}