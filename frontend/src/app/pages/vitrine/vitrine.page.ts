import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { IonContent, IonHeader, IonTitle, IonToolbar } from '@ionic/angular';
import { signal, inject } from '@angular/core';
import { HealthService } from '../../services/health.service';

@Component({
  selector: 'app-vitrine',
  templateUrl: './vitrine.page.html',
  styleUrls: ['./vitrine.page.scss'],
  imports: [IonContent, IonHeader, IonTitle, IonToolbar, CommonModule, FormsModule]
})
export class VitrinePage implements OnInit {
  private health = inject(HealthService);
  apiStatus = signal('verificando...');
  
  constructor() { }

  ngOnInit() {
  this.health.check().subscribe({
    next: r => this.apiStatus.set(r.status),
    error: () => this.apiStatus.set('API fora do ar'),
  });
}

}
