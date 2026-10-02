import { Routes } from '@angular/router';
import { TabsPage } from './tabs.page';

export const routes: Routes = [
  {
    path: 'tabs',
    component: TabsPage,
    children: [
      { path: 'vitrine',  loadComponent: () => import('../pages/vitrine/vitrine.page').then(m => m.VitrinePage) },
      { path: 'carrinho', loadComponent: () => import('../pages/carrinho/carrinho.page').then(m => m.CarrinhoPage) },
      { path: 'pedidos',  loadComponent: () => import('../pages/pedidos/pedidos.page').then(m => m.PedidosPage) },
      { path: 'conta',    loadComponent: () => import('../pages/conta/conta.page').then(m => m.ContaPage) },
      { path: '', redirectTo: '/tabs/vitrine', pathMatch: 'full' },
    ],
  },
  { path: '', redirectTo: '/tabs/vitrine', pathMatch: 'full' },
];