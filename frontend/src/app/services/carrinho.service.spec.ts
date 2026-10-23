import { TestBed } from '@angular/core/testing';
import { CarrinhoService, MAX_POR_ITEM } from './carrinho.service';
import { ProdutoResponse } from '../models/produto.model';

const CHAVE = 'cupcakes.carrinho';

const produto = (id: number, preco = 10): ProdutoResponse => ({
  id, nome: `Cupcake ${id}`, descricao: '', preco, fotoUrl: `/img/${id}.jpg`,
  estoque: 10, categoriaId: 1, categoriaNome: 'Clássicos', disponivel: true,
});

describe('CarrinhoService', () => {
  // O service lê o localStorage AO SER CRIADO, por isso injetamos dentro de cada teste
  const criar = () => TestBed.inject(CarrinhoService);

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({});
  });

  it('deve adicionar um produto novo com quantidade 1', () => {
    const carrinho = criar();
    carrinho.adicionar(produto(1));

    expect(carrinho.itens().length).toBe(1);
    expect(carrinho.itens()[0].quantidade).toBe(1);
  });

  it('deve somar a quantidade ao adicionar o mesmo produto, sem duplicar', () => {
    const carrinho = criar();
    carrinho.adicionar(produto(1), 2);
    carrinho.adicionar(produto(1), 3);

    expect(carrinho.itens().length).toBe(1);
    expect(carrinho.itens()[0].quantidade).toBe(5);
  });

  it('deve limitar a 50 unidades por item e avisar (RN#1)', () => {
    const carrinho = criar();
    carrinho.adicionar(produto(1), 48);
    const limitou = carrinho.adicionar(produto(1), 5);

    expect(limitou).toBe(true);
    expect(carrinho.itens()[0].quantidade).toBe(MAX_POR_ITEM);
  });

  it('não deve avisar limite quando fica dentro do máximo', () => {
    const carrinho = criar();
    expect(carrinho.adicionar(produto(1), 50)).toBe(false);
  });

  it('deve calcular total e quantidade total', () => {
    const carrinho = criar();
    carrinho.adicionar(produto(1, 10), 2);   // 20
    carrinho.adicionar(produto(2, 7.5), 4);  // 30

    expect(carrinho.total()).toBe(50);
    expect(carrinho.quantidadeTotal()).toBe(6);
  });

  it('deve alterar a quantidade respeitando o máximo', () => {
    const carrinho = criar();
    carrinho.adicionar(produto(1));
    carrinho.alterarQuantidade(1, 99);

    expect(carrinho.itens()[0].quantidade).toBe(MAX_POR_ITEM);
  });

  it('deve remover o item quando a quantidade for alterada para 0', () => {
    const carrinho = criar();
    carrinho.adicionar(produto(1));
    carrinho.alterarQuantidade(1, 0);

    expect(carrinho.itens().length).toBe(0);
  });

  it('deve remover retornando o item, e restaurar com o Desfazer (US-07)', () => {
    const carrinho = criar();
    carrinho.adicionar(produto(1), 3);

    const removido = carrinho.remover(1);
    expect(carrinho.itens().length).toBe(0);
    expect(removido?.quantidade).toBe(3);

    carrinho.restaurar(removido!);
    expect(carrinho.itens()[0].quantidade).toBe(3);
  });

  it('deve salvar o carrinho no localStorage (RNF#1)', () => {
    const carrinho = criar();
    carrinho.adicionar(produto(1), 2);
    TestBed.tick();   // força o effect a rodar

    const salvo = JSON.parse(localStorage.getItem(CHAVE)!);
    expect(salvo.itens[0].quantidade).toBe(2);
  });

  it('deve carregar o carrinho salvo há menos de 24h', () => {
    localStorage.setItem(CHAVE, JSON.stringify({
      itens: [{ produtoId: 1, nome: 'X', preco: 10, fotoUrl: '', quantidade: 4 }],
      salvoEm: Date.now() - 60 * 60 * 1000,   // 1h atrás
    }));

    expect(criar().itens()[0].quantidade).toBe(4);
  });

  it('deve descartar o carrinho salvo há mais de 24h (RNF#1)', () => {
    localStorage.setItem(CHAVE, JSON.stringify({
      itens: [{ produtoId: 1, nome: 'X', preco: 10, fotoUrl: '', quantidade: 4 }],
      salvoEm: Date.now() - 25 * 60 * 60 * 1000,   // 25h atrás
    }));

    expect(criar().itens().length).toBe(0);
  });

  it('não deve quebrar com dado corrompido no localStorage', () => {
    localStorage.setItem(CHAVE, '{isso não é json');

    expect(criar().itens()).toEqual([]);
  });
});