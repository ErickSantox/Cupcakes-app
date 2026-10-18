import { Component, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import {
  IonContent, IonHeader, IonTitle, IonToolbar, IonButtons, IonBackButton,
  IonInput, IonButton, IonSpinner, ToastController,
} from '@ionic/angular';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.page.html',
  styleUrls: ['./login.page.scss'],
  imports: [
    ReactiveFormsModule, RouterLink,
    IonContent, IonHeader, IonTitle, IonToolbar, IonButtons, IonBackButton,
    IonInput, IonButton, IonSpinner,
  ],
})
export class LoginPage {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);
  private toast = inject(ToastController);

  form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    senha: ['', [Validators.required]],
  });

  enviando = signal(false);

  entrar() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.enviando.set(true);

    const { email, senha } = this.form.getRawValue();

    this.authService.login({ email: email.trim(), senha })
      .pipe(finalize(() => this.enviando.set(false)))
      .subscribe({
        next: async (resposta) => {
          const t = await this.toast.create({
            message: `Bem-vindo(a), ${resposta.usuario.nome}!`,
            duration: 2000, color: 'success', position: 'top',
          });
          await t.present();
          this.form.reset();
          this.router.navigateByUrl('/tabs/vitrine');   // US-02 CA#1: login válido → home
        },
        // erros (401 senha errada, 423 bloqueado) → o interceptor mostra o toast
      });
  }
}