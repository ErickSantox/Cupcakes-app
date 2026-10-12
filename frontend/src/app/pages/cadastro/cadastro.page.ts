import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { IonContent, IonHeader, IonTitle, IonToolbar, IonInput, IonButton, IonSpinner, IonBackButton, IonButtons, ToastController } from '@ionic/angular';
import { AuthService } from '../../services/auth.service';
import { CadastroRequest } from '../../models/usuario.model';
import { Router } from '@angular/router';
import { finalize } from "rxjs";

@Component({
  selector: 'app-cadastro',
  templateUrl: './cadastro.page.html',
  styleUrls: ['./cadastro.page.scss'],
  imports: [IonContent, IonHeader, IonTitle, IonToolbar, CommonModule, FormsModule, ReactiveFormsModule, IonInput, IonButton, IonSpinner, IonBackButton, IonButtons]
})
export class CadastroPage implements OnInit {


  constructor() { }

  ngOnInit() {
  }

  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);
  private toast = inject(ToastController);

  form = this.fb.nonNullable.group({
    nome: ['', [Validators.required, Validators.minLength(2)]],
    sobrenome: ['', [Validators.required, Validators.minLength(2)]],
    telefone: ['', [Validators.required, Validators.pattern(/^\d{10,11}$/)]],
    email: ['', [Validators.required, Validators.email]],
    senha: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(72)]],
  });

  enviando = signal(false);

  cadastrar() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.enviando.set(true);

    const dados: CadastroRequest = {
      ...this.form.getRawValue(),
      email: this.form.getRawValue().email.trim(),
    };

    this.authService.cadastrar(dados)
      .pipe(finalize(() => this.enviando.set(false)))
      .subscribe({
        next: async () => {
          const t = await this.toast.create({
            message: 'Conta criada com sucesso! Faça login para continuar.',
            duration: 3000, color: 'success', position: 'top',
          });
          await t.present();
          this.form.reset();
          this.router.navigateByUrl('/tabs/conta');
        },
        error: (err) => {
          const campos = err.error?.campos as Record<string, string> | null;
          if (err.status === 400 && campos) {
            Object.entries(campos).forEach(([campo, msg]) =>
              this.form.get(campo)?.setErrors({ servidor: msg }));
            this.form.markAllAsTouched();
          }
        },
      });
  }

  erroDe(campo: string, padrao: string): string {
    return this.form.get(campo)?.errors?.['servidor'] ?? padrao;
  }

}
