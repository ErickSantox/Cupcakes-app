import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { AdminProdutosPage } from './admin-produtos.page';

describe('AdminProdutosPage', () => {
  let component: AdminProdutosPage;
  let fixture: ComponentFixture<AdminProdutosPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    }).compileComponents();
    fixture = TestBed.createComponent(AdminProdutosPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('deve invalidar preço zero e foto vazia (RN#1 e RN#2)', () => {
    component.form.patchValue({ preco: 0, fotoUrl: '' });
    expect(component.form.controls.preco.invalid).toBe(true);
    expect(component.form.controls.fotoUrl.invalid).toBe(true);
  });
});