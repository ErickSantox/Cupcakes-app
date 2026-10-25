export interface ProdutoResponse {
    id: number;
    nome: string;
    descricao: string;
    preco: number;
    fotoUrl: string;
    estoque: number;
    categoriaId: number;
    categoriaNome: string;
    disponivel: boolean;
    ativo: boolean;
}

export interface CategoriaResponse {
    id: number;
    nome: string;
}

export interface ProdutoRequest {
    nome: string;
    descricao: string;
    preco: number;
    categoriaId: number;
    fotoUrl: string;
    estoque: number;
}