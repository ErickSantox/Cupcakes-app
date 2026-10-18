import { Component, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import {
  IonContent, IonHeader, IonTitle, IonToolbar, IonButton, IonRouterLink, ToastController,
} from '@ionic/angular';
import { AuthService } from '../../services/auth.service';
import { SessaoService } from '../../services/sessao.service';

@Component({
  selector: 'app-conta',
  templateUrl: './conta.page.html',
  styleUrls: ['./conta.page.scss'],
  imports: [IonContent, IonHeader, IonTitle, IonToolbar, IonButton, IonRouterLink, RouterLink],
})
export class ContaPage {
  sessao = inject(SessaoService);
  private authService = inject(AuthService);
  private router = inject(Router);
  private toast = inject(ToastController);

  async sair() {
    this.authService.logout();
    const t = await this.toast.create({
      message: 'Você saiu da sua conta.', duration: 2000, position: 'top',
    });
    await t.present();
    this.router.navigateByUrl('/login');   // US-03 CA#2: redireciona para o login
  }
}