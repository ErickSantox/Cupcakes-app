import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { ContaPage } from './conta.page';
import { SessaoService } from '../../services/sessao.service';

describe('ContaPage', () => {
  let fixture: ComponentFixture<ContaPage>;

  beforeEach(async () => {
    localStorage.clear();
    await TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    }).compileComponents();
  });

  function criar() {
    fixture = TestBed.createComponent(ContaPage);
    fixture.detectChanges();
    return fixture.nativeElement as HTMLElement;
  }

  it('should create', () => {
    criar();
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('deve mostrar "Entrar" quando ninguém está logado', () => {
    const tela = criar();
    expect(tela.textContent).toContain('Entrar');
  });

  it('deve mostrar o nome do usuário quando está logado', () => {
    TestBed.inject(SessaoService).salvar({
      token: 'token-falso',
      expiraEm: new Date(Date.now() + 60_000).toISOString(),
      usuario: { id: 1, nome: 'Erick', sobrenome: 'Santos', telefone: '11999998888',
                 email: 'erick@teste.com', perfil: 'CLIENTE' },
    });

    const tela = criar();
    expect(tela.textContent).toContain('Olá, Erick!');
  });
});