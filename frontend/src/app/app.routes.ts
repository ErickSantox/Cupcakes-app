import { Routes } from '@angular/router';
import { adminGuard } from './guards/admin.guard';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    loadChildren: () => import('./tabs/tabs.routes').then((m) => m.routes),
  },
  {
    path: 'cadastro',
    loadComponent: () => import('./pages/cadastro/cadastro.page').then( m => m.CadastroPage)
  },
  {
    path: 'login',
    loadComponent: () => import('./pages/login/login.page').then( m => m.LoginPage)
  },
  {
    path: 'produto/:id',
    loadComponent: () => import('./pages/produto-detalhe/produto-detalhe.page').then( m => m.ProdutoDetalhePage)
  },
    {
    path: 'admin/produtos',
    canActivate: [adminGuard],
    loadComponent: () => import('./pages/admin-produtos/admin-produtos.page').then(m => m.AdminProdutosPage),
  },
    {
    path: 'enderecos',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/enderecos/enderecos.page').then(m => m.EnderecosPage),
  },
  {
    path: 'checkout',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/checkout/checkout.page').then(m => m.CheckoutPage),
  },
  {
    path: 'pedido/:id',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/pedido-confirmacao/pedido-confirmacao.page').then(m => m.PedidoConfirmacaoPage),
  },
];
