import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { CarrinhoPage } from './carrinho.page';
import { CarrinhoService } from '../../services/carrinho.service';

describe('CarrinhoPage', () => {
  let fixture: ComponentFixture<CarrinhoPage>;
  let carrinho: CarrinhoService;

  beforeEach(async () => {
    localStorage.clear();
    await TestBed.configureTestingModule({ providers: [provideRouter([])] }).compileComponents();
    carrinho = TestBed.inject(CarrinhoService);
  });

  const renderizar = () => {
    fixture = TestBed.createComponent(CarrinhoPage);
    fixture.detectChanges();
    return fixture.nativeElement as HTMLElement;
  };

  it('deve mostrar mensagem de carrinho vazio (US-07 RN#1)', () => {
    expect(renderizar().textContent).toContain('Seu carrinho está vazio');
  });

  it('deve listar os itens do carrinho', () => {
    carrinho.adicionar({ id: 1, nome: 'Brigadeiro', descricao: '', preco: 10, fotoUrl: '',
      estoque: 5, categoriaId: 1, categoriaNome: 'X', disponivel: true }, 2);

    const tela = renderizar();
    expect(tela.textContent).toContain('Brigadeiro');
    expect(tela.textContent).not.toContain('Seu carrinho está vazio');
  });
});