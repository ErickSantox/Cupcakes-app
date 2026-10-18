import { Injectable, computed, signal } from '@angular/core';
import { LoginResponse, UsuarioResponse } from '../models/usuario.model';

const CHAVE_TOKEN = 'cupcakes.token';
const CHAVE_EXPIRA = 'cupcakes.expiraEm';
const CHAVE_USUARIO = 'cupcakes.usuario';

@Injectable({ providedIn: 'root' })
export class SessaoService {
  private readonly _usuario = signal<UsuarioResponse | null>(this.carregarUsuario());

  /** Usuário logado (ou null). As telas leem, mas só este service altera. */
  readonly usuario = this._usuario.asReadonly();
  readonly logado = computed(() => this._usuario() !== null);

  salvar(resposta: LoginResponse): void {
    localStorage.setItem(CHAVE_TOKEN, resposta.token);
    localStorage.setItem(CHAVE_EXPIRA, resposta.expiraEm);
    localStorage.setItem(CHAVE_USUARIO, JSON.stringify(resposta.usuario));
    this._usuario.set(resposta.usuario);
  }

  /** Devolve o token só se ainda estiver válido; se expirou, limpa a sessão. */
  token(): string | null {
    if (this.expirou()) {
      this.limpar();
      return null;
    }
    return localStorage.getItem(CHAVE_TOKEN);
  }

  limpar(): void {
    localStorage.removeItem(CHAVE_TOKEN);
    localStorage.removeItem(CHAVE_EXPIRA);
    localStorage.removeItem(CHAVE_USUARIO);
    this._usuario.set(null);
  }

  private expirou(): boolean {
    const expira = localStorage.getItem(CHAVE_EXPIRA);
    return !expira || new Date(expira) <= new Date();
  }

  private carregarUsuario(): UsuarioResponse | null {
    if (this.expirou()) return null;
    const json = localStorage.getItem(CHAVE_USUARIO);
    return json ? JSON.parse(json) : null;
  }
}