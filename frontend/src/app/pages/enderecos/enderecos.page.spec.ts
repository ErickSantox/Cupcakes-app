import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { EnderecosPage } from './enderecos.page';

describe('EnderecosPage', () => {
  let component: EnderecosPage;
  let fixture: ComponentFixture<EnderecosPage>;
  let http: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    }).compileComponents();
    fixture = TestBed.createComponent(EnderecosPage);
    component = fixture.componentInstance;
    http = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
    http.expectOne(r => r.url.endsWith('/enderecos')).flush([]);   // listagem do ngOnInit
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('deve preencher o endereço pelo ViaCEP (RNF#1)', () => {
    component.onCep(new CustomEvent('ionInput', { detail: { value: '01310-100' } }));

    http.expectOne('https://viacep.com.br/ws/01310100/json/').flush({
      logradouro: 'Avenida Paulista', bairro: 'Bela Vista', localidade: 'São Paulo', uf: 'SP',
    });

    expect(component.form.controls.logradouro.value).toBe('Avenida Paulista');
    expect(component.form.controls.cidade.value).toBe('São Paulo');
    http.verify();
  });

  it('deve avisar quando o CEP não existe', () => {
    component.onCep(new CustomEvent('ionInput', { detail: { value: '99999999' } }));
    http.expectOne(r => r.url.includes('viacep')).flush({ erro: true });

    expect(component.avisoCep()).toContain('CEP não encontrado');
  });

  it('não deve chamar o ViaCEP com CEP incompleto', () => {
    component.onCep(new CustomEvent('ionInput', { detail: { value: '0131' } }));
    http.verify();   // falha se alguma requisição tiver saído
  });
});