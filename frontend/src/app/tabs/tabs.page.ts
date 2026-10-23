import { Component, EnvironmentInjector, inject } from '@angular/core';
import { IonTabs, IonTabBar, IonTabButton, IonIcon, IonLabel,IonBadge } from '@ionic/angular';
import { addIcons } from 'ionicons';
import { storefrontOutline, cartOutline, receiptOutline, personOutline } from 'ionicons/icons';
import { CarrinhoService } from '../services/carrinho.service';


@Component({
  selector: 'app-tabs',
  templateUrl: 'tabs.page.html',
  styleUrls: ['tabs.page.scss'],
  imports: [IonTabs, IonTabBar, IonTabButton, IonIcon, IonLabel,IonBadge],
})
export class TabsPage {
  public environmentInjector = inject(EnvironmentInjector);

  constructor() {
    addIcons({ storefrontOutline, cartOutline, receiptOutline, personOutline });
  }

  carrinho = inject(CarrinhoService);
}
