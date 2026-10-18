import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { ToastController } from '@ionic/angular';
import { catchError, throwError } from 'rxjs';
import { SessaoService } from '../services/sessao.service';
import { Router } from '@angular/router';

export const erroInterceptor: HttpInterceptorFn = (req, next) => {
  const toast = inject(ToastController);
  const sessao = inject(SessaoService);
  const router = inject(Router);

  return next(req).pipe(
    catchError((err: HttpErrorResponse) => {
      const ehTentativaDeLogin = req.url.includes('/auth/login');
      let mensagem: string | null = null;

      if (err.status === 401 && !ehTentativaDeLogin) {
        sessao.limpar();
        router.navigateByUrl('/login');
        mensagem = 'Sua sessão expirou. Faça login novamente.';
      } else if (!err.error?.campos) {
        mensagem = err.status === 0
          ? 'Não foi possível conectar. Verifique sua internet.'
          : err.error?.mensagem ?? 'Erro inesperado. Tente novamente.';
      }

      if (mensagem) {
        toast.create({ message: mensagem, duration: 3000, color: 'danger', position: 'top' })
          .then(t => t.present());
      }
      return throwError(() => err);
    })
  );
};