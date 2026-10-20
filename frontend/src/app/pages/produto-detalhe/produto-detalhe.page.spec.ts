import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ProdutoDetalhePage } from './produto-detalhe.page';

describe('ProdutoDetalhePage', () => {
  let component: ProdutoDetalhePage;
  let fixture: ComponentFixture<ProdutoDetalhePage>;

  beforeEach(() => {
    fixture = TestBed.createComponent(ProdutoDetalhePage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
