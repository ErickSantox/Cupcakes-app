import { Component, OnInit, inject, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import {
  IonContent, IonHeader, IonTitle, IonToolbar, IonButtons, IonBackButton, IonButton, IonIcon,
  IonList, IonItem, IonThumbnail, IonImg, IonLabel, IonToggle, IonModal, IonInput, IonTextarea,
  IonSelect, IonSelectOption, IonNote, ToastController,
} from '@ionic/angular';
import { addIcons } from 'ionicons';
import { add, createOutline } from 'ionicons/icons';
import { AdminProdutoService } from '../../services/admin-produto.service';
import { ProdutoService } from '../../services/produto.service';
import { CategoriaResponse, ProdutoResponse } from '../../models/produto.model';
import { urlDaFoto } from '../../utils/foto';

@Component({
  selector: 'app-admin-produtos',
  templateUrl: './admin-produtos.page.html',
  styleUrls: ['./admin-produtos.page.scss'],
  imports: [
    CurrencyPipe, ReactiveFormsModule,
    IonContent, IonHeader, IonTitle, IonToolbar, IonButtons, IonBackButton, IonButton, IonIcon,
    IonList, IonItem, IonThumbnail, IonImg, IonLabel, IonToggle, IonModal, IonInput, IonTextarea,
    IonSelect, IonSelectOption, IonNote,
  ],
})
export class AdminProdutosPage implements OnInit {
  private admin = inject(AdminProdutoService);
  private produtoService = inject(ProdutoService);
  private toast = inject(ToastController);
  private fb = inject(FormBuilder);

  produtos = signal<ProdutoResponse[]>([]);
  categorias = signal<CategoriaResponse[]>([]);
  modalAberto = signal(false);
  editandoId = signal<number | null>(null);
  salvando = signal(false);

  readonly urlDaFoto = urlDaFoto;

  form = this.fb.nonNullable.group({
    nome: ['', [Validators.required, Validators.maxLength(100)]],
    descricao: ['', [Validators.required, Validators.maxLength(200)]],
    preco: [0, [Validators.required, Validators.min(0.01)]],         // RN#1
    categoriaId: [0, [Validators.required, Validators.min(1)]],
    fotoUrl: ['', [Validators.required, Validators.maxLength(255)]], // RN#2
    estoque: [0, [Validators.required, Validators.min(0)]],
  });

  constructor() {
    addIcons({ add, createOutline });
  }

  ngOnInit() {
    this.produtoService.categorias().subscribe(c => this.categorias.set(c));
    this.carregar();
  }

  carregar() {
    this.admin.listar().subscribe(p => this.produtos.set(p));
  }

  novo() {
    this.editandoId.set(null);
    this.form.reset();
    this.modalAberto.set(true);
  }

  editar(p: ProdutoResponse) {
    this.editandoId.set(p.id);
    this.form.setValue({
      nome: p.nome, descricao: p.descricao, preco: p.preco,
      categoriaId: p.categoriaId, fotoUrl: p.fotoUrl, estoque: p.estoque,
    });
    this.modalAberto.set(true);
  }

  salvar() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.salvando.set(true);
    const req = this.form.getRawValue();
    const id = this.editandoId();
    const chamada = id ? this.admin.atualizar(id, req) : this.admin.criar(req);

    chamada.subscribe({
      next: () => {
        this.salvando.set(false);
        this.modalAberto.set(false);
        this.carregar();
        this.mostrar(id ? 'Produto atualizado.' : 'Produto cadastrado! Já está na vitrine. 🧁', 'success');
      },
      error: (e) => {
        this.salvando.set(false);
        // 400 com "campos": o erroInterceptor não mostra toast, então mostramos aqui
        const campos = e.error?.campos;
        if (campos) this.mostrar(Object.values(campos)[0] as string, 'danger');
      },
    });
  }

  alternarAtivo(p: ProdutoResponse, ativo: boolean) {
    this.admin.alterarAtivo(p.id, ativo).subscribe({
      next: () => {
        this.carregar();
        this.mostrar(ativo ? `${p.nome} voltou para a vitrine.` : `${p.nome} saiu da vitrine.`, 'medium');
      },
      error: () => this.carregar(),   // desfaz o toggle visualmente se falhar
    });
  }

  fotoPadrao(evento: Event) {
    (evento.target as HTMLIonImgElement).src = 'assets/img/logo.png';
  }

  private async mostrar(message: string, color: string) {
    const t = await this.toast.create({ message, color, duration: 2500, position: 'top' });
    await t.present();
  }
}