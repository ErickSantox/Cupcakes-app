export interface ErroResponse {
    status: number;
    erro: string;
    mensagem: string;
    campos: Record<string, string> | null;
    timestamp: string;
}