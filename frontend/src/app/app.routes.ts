import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadChildren: () => import('./tabs/tabs.routes').then((m) => m.routes),
  },
  {
    path: 'vitrine',
    loadComponent: () => import('./pages/vitrine/vitrine.page').then( m => m.VitrinePage)
  },
  {
    path: 'carrinho',
    loadComponent: () => import('./pages/carrinho/carrinho.page').then( m => m.CarrinhoPage)
  },
  {
    path: 'pedidos',
    loadComponent: () => import('./pages/pedidos/pedidos.page').then( m => m.PedidosPage)
  },
  {
    path: 'conta',
    loadComponent: () => import('./pages/conta/conta.page').then( m => m.ContaPage)
  },
];
