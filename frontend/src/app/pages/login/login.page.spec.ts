import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { LoginPage } from './login.page';

describe('LoginPage', () => {
  let component: LoginPage;
  let fixture: ComponentFixture<LoginPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(LoginPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('deve começar com o formulário inválido', () => {
    expect(component.form.invalid).toBe(true);
  });

  it('deve recusar e-mail em formato inválido', () => {
    component.form.controls.email.setValue('nao-e-email');
    expect(component.form.controls.email.hasError('email')).toBe(true);
  });

  it('deve ficar válido com e-mail e senha preenchidos', () => {
    component.form.setValue({ email: 'erick@teste.com', senha: 'senha123' });
    expect(component.form.valid).toBe(true);
  });
});