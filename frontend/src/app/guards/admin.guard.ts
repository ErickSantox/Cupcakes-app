import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { SessaoService } from '../services/sessao.service';

/** Só deixa entrar quem está logado como ADMIN */
export const adminGuard: CanActivateFn = () => {
  const sessao = inject(SessaoService);
  return sessao.usuario()?.perfil === 'ADMIN' ? true : inject(Router).parseUrl('/login');
};