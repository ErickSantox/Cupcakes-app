import { Component, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import {
  IonContent, IonHeader, IonTitle, IonToolbar, IonButtons, IonBackButton,
  IonInput, IonButton, IonSpinner, IonNote, ToastController,
} from '@ionic/angular';
import { AuthService } from '../../services/auth.service';

/** As duas senhas digitadas precisam ser iguais */
function senhasIguais(grupo: AbstractControl): ValidationErrors | null {
  const { novaSenha, confirmacao } = grupo.value;
  return confirmacao && novaSenha !== confirmacao ? { senhasDiferentes: true } : null;
}

@Component({
  selector: 'app-redefinir-senha',
  templateUrl: './redefinir-senha.page.html',
  styleUrls: ['./redefinir-senha.page.scss'],
  imports: [
    ReactiveFormsModule, RouterLink,
    IonContent, IonHeader, IonTitle, IonToolbar, IonButtons, IonBackButton,
    IonInput, IonButton, IonSpinner, IonNote,
  ],
})
export class RedefinirSenhaPage {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);
  private toast = inject(ToastController);

  /** Vem do link: /redefinir-senha?token=... */
  readonly token = inject(ActivatedRoute).snapshot.queryParamMap.get('token') ?? '';

  form = this.fb.nonNullable.group({
    novaSenha: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(72)]],
    confirmacao: ['', [Validators.required]],
  }, { validators: senhasIguais });

  enviando = signal(false);

  redefinir() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.enviando.set(true);

    this.authService.redefinirSenha(this.token, this.form.getRawValue().novaSenha)
      .pipe(finalize(() => this.enviando.set(false)))
      .subscribe({
        next: async () => {
          const t = await this.toast.create({
            message: 'Senha alterada! Faça login com a nova senha.',
            duration: 3000, color: 'success', position: 'top',
          });
          await t.present();
          this.router.navigateByUrl('/login', { replaceUrl: true });
        },
        // 422 (link inválido, usado ou expirado) → o erroInterceptor mostra o toast
      });
  }
}
