import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { ToastController } from '@ionic/angular';
import { catchError, throwError } from 'rxjs';

export const erroInterceptor: HttpInterceptorFn = (req, next) => {
  const toast = inject(ToastController);
  return next(req).pipe(
    catchError((err: HttpErrorResponse) => {
      const temErrosDeCampo = !!err.error?.campos;

      if (!temErrosDeCampo) {
        const mensagem = err.status === 0
          ? 'Não foi possível conectar. Verifique sua internet.'
          : err.error?.mensagem ?? 'Erro inesperado. Tente novamente.';
        toast.create({ message: mensagem, duration: 3000, color: 'danger', position: 'top' })
          .then(t => t.present());
      }

      return throwError(() => err);
    })
  );
};