

export interface CadastroRequest {
    nome: string;
    sobrenome: string;
    telefone: string;
    email: string;
    senha: string;
}

export interface UsuarioResponse {
    id: number;
    nome: string;
    sobrenome: string;
    telefone: string;
    email: string;
    perfil: 'CLIENTE' | 'ADMIN';
}

