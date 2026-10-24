/** Seed usa "/img/x.jpg" (assets locais); admin cadastra URL completa "https://..." */
export function urlDaFoto(fotoUrl: string): string {
  return fotoUrl.startsWith('http') ? fotoUrl : `assets${fotoUrl}`;
}