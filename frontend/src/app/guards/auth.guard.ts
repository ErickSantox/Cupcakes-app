import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { SessaoService } from '../services/sessao.service';

/** Só deixa entrar quem está logado */
export const authGuard: CanActivateFn = () =>
  inject(SessaoService).logado() ? true : inject(Router).parseUrl('/login');