import { ComponentFixture, TestBed } from '@angular/core/testing';
import { VitrinePage } from './vitrine.page';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { HttpTestingController } from '@angular/common/http/testing';

describe('VitrinePage', () => {
  let component: VitrinePage;
  let fixture: ComponentFixture<VitrinePage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      providers: [provideHttpClient(),provideHttpClientTesting(), provideRouter([])],
    }).compileComponents();
    fixture = TestBed.createComponent(VitrinePage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

    it('deve buscar produtos com o termo digitado (US-05)', () => {
    const http = TestBed.inject(HttpTestingController);

    // As 2 requisições iniciais do ngOnInit (categorias e produtos)
    http.expectOne(r => r.url.endsWith('/categorias')).flush([]);
    http.expectOne(r => r.url.endsWith('/produtos')).flush([]);

    // O usuário digita "choco"
    component.onBusca(new CustomEvent('ionInput', { detail: { value: 'choco' } }));

    // Esperamos uma requisição com ?busca=choco
    const req = http.expectOne(r => r.url.endsWith('/produtos') && r.params.get('busca') === 'choco');
    req.flush([]);

    expect(component.busca()).toBe('choco');
    http.verify();   // garante que não sobrou nenhuma requisição "perdida"
  });
});
