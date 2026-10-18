

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

export interface LoginRequest {
    email: string;
    senha: string;


}

export interface LoginResponse {
    token: string;
    expiraEm: string; //Instant do Java chega como texto ISO:"2023-06-30T15:30:00Z"
    usuario: UsuarioResponse;
}




