import { Component, ElementRef, OnInit, inject, signal, viewChild } from '@angular/core';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import {
  IonContent, IonHeader, IonTitle, IonToolbar, IonButtons, IonBackButton, IonButton, IonIcon,
} from '@ionic/angular';
import { addIcons } from 'ionicons';
import { printOutline } from 'ionicons/icons';
import { AdminPedidoService } from '../../services/admin-pedido.service';
import { PedidoResponse, STATUS_LABEL } from '../../models/pedido.model';

/** CSS próprio do comprovante impresso (o CSS do componente não vai para dentro do iframe) */
const ESTILO_IMPRESSAO = `
  @page { size: A4; margin: 15mm; }
  body { font-family: Arial, Helvetica, sans-serif; font-size: 13px; color: #000; }
  header { text-align: center; border-bottom: 1px dashed #999; margin-bottom: 12px; padding-bottom: 8px; }
  h1 { margin: 0; font-size: 22px; }
  h2 { font-size: 15px; margin: 16px 0 4px; }
  p { margin: 2px 0; }
  table { width: 100%; border-collapse: collapse; }
  th, td { text-align: left; padding: 5px 4px; border-bottom: 1px solid #ccc; }
  .totais { text-align: right; margin-top: 12px; }
  .total { font-size: 17px; font-weight: bold; }
`;

@Component({
  selector: 'app-comprovante',
  templateUrl: './comprovante.page.html',
  styleUrls: ['./comprovante.page.scss'],
  imports: [
    CurrencyPipe, DatePipe,
    IonContent, IonHeader, IonTitle, IonToolbar, IonButtons, IonBackButton, IonButton, IonIcon,
  ],
})
export class ComprovantePage implements OnInit {
  private route = inject(ActivatedRoute);
  private service = inject(AdminPedidoService);

  readonly STATUS_LABEL = STATUS_LABEL;
  pedido = signal<PedidoResponse | null>(null);
  recibo = viewChild<ElementRef<HTMLElement>>('recibo');

  constructor() {
    addIcons({ printOutline });
  }

  ngOnInit() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.service.buscar(id).subscribe(p => this.pedido.set(p));
  }

  /** Imprime num iframe invisível, longe do layout do Ionic (shadow DOM + position absolute) */
  imprimir() {
    const conteudo = this.recibo()?.nativeElement.innerHTML;
    if (!conteudo) return;

    const iframe = document.createElement('iframe');
    iframe.style.cssText = 'position:fixed;width:0;height:0;border:0;';
    document.body.appendChild(iframe);

    const doc = iframe.contentDocument!;
    doc.open();
    doc.write(`<!doctype html><html><head><meta charset="utf-8">
      <title>Comprovante ${this.pedido()?.numero}</title>
      <style>${ESTILO_IMPRESSAO}</style></head><body>${conteudo}</body></html>`);
    doc.close();

    const janela = iframe.contentWindow!;
    janela.onafterprint = () => iframe.remove();
    janela.focus();
    janela.print();
  }
}