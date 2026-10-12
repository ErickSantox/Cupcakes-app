import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { IonContent, IonHeader, IonTitle, IonToolbar, IonRouterLink, IonButton } from '@ionic/angular';
import {RouterLink} from "@angular/router";

@Component({
  selector: 'app-conta',
  templateUrl: './conta.page.html',
  styleUrls: ['./conta.page.scss'],
  imports: [IonContent, IonHeader, IonTitle, IonToolbar, CommonModule, FormsModule, IonRouterLink, IonButton, RouterLink]
})
export class ContaPage implements OnInit {

  constructor() { }

  ngOnInit() {
  }

}
