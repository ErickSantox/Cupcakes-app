import { Component, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import {
  IonContent, IonHeader, IonTitle, IonToolbar, IonButtons, IonBackButton,
  IonInput, IonButton, IonSpinner, IonIcon,
} from '@ionic/angular';
import { addIcons } from 'ionicons';
import { mailOutline } from 'ionicons/icons';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-esqueci-senha',
  templateUrl: './esqueci-senha.page.html',
  styleUrls: ['./esqueci-senha.page.scss'],
  imports: [
    ReactiveFormsModule, RouterLink,
    IonContent, IonHeader, IonTitle, IonToolbar, IonButtons, IonBackButton,
    IonInput, IonButton, IonSpinner, IonIcon,
  ],
})
export class EsqueciSenhaPage {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);

  form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
  });

  enviando = signal(false);
  enviado = signal(false);

  constructor() {
    addIcons({ mailOutline });
  }

  enviar() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.enviando.set(true);

    this.authService.esqueciSenha(this.form.getRawValue().email.trim())
      .pipe(finalize(() => this.enviando.set(false)))
      .subscribe(() => this.enviado.set(true));   // mesma tela para qualquer e-mail (não revela quem tem conta)
  }
}
