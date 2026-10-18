import { TestBed } from '@angular/core/testing';
import { SessaoService } from './sessao.service';
import { LoginResponse } from '../models/usuario.model';

function resposta(expiraEmMs: number): LoginResponse {
  return {
    token: 'token-falso',
    expiraEm: new Date(Date.now() + expiraEmMs).toISOString(),
    usuario: { id: 1, nome: 'Erick', sobrenome: 'Santos', telefone: '11999998888',
               email: 'erick@teste.com', perfil: 'CLIENTE' },
  };
}

describe('SessaoService', () => {
  let sessao: SessaoService;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({});
    sessao = TestBed.inject(SessaoService);
  });

  it('deve começar deslogado', () => {
    expect(sessao.logado()).toBe(false);
    expect(sessao.token()).toBeNull();
  });

  it('deve salvar a sessão e devolver o token válido', () => {
    sessao.salvar(resposta(60_000));               // expira em 1 minuto
    expect(sessao.logado()).toBe(true);
    expect(sessao.usuario()?.nome).toBe('Erick');
    expect(sessao.token()).toBe('token-falso');
  });

  // US-02 RN#2: sessão expira
  it('não deve devolver token expirado e deve limpar a sessão', () => {
    sessao.salvar(resposta(-1_000));               // expirou há 1 segundo
    expect(sessao.token()).toBeNull();
    expect(sessao.logado()).toBe(false);
    expect(localStorage.getItem('cupcakes.token')).toBeNull();
  });

  // US-03 RN#1: logout apaga todos os dados de sessão
  it('deve apagar todos os dados ao limpar (logout)', () => {
    sessao.salvar(resposta(60_000));
    sessao.limpar();
    expect(sessao.logado()).toBe(false);
    expect(localStorage.length).toBe(0);
  });
});