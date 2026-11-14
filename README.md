# 🧁 Cupcakes App

Aplicativo de loja virtual de cupcakes gourmet: vitrine, carrinho, pedido, pagamento (cartão e PIX) e acompanhamento da entrega, com painel administrativo para a loja.

Projeto desenvolvido no **Projeto Integrador Transdisciplinar em Engenharia de Software II** (Cruzeiro do Sul Virtual), a partir dos requisitos ágeis levantados no PIT I ([User Stories](docs/pit-1/pit1-requisitos-ageis.pdf)).

## Acesse

| | |
|---|---|
| Aplicação | https://cupcakes-web.onrender.com |
| Vídeo: demonstração do sistema | https://www.youtube.com/watch?v=Vt-E3U0t1f0 |
| Vídeo: correções após os testes | https://youtu.be/4vU0inldO4E |

Os pagamentos são simulados. Para testar, use o cartão `4111 1111 1111 1111` (aprovado) ou `4000 0000 0000 0002` (recusado), com qualquer validade futura e CVV. Veja o [manual de uso](docs/situacao-2/README.md).

> Hospedado no plano gratuito do Render: o primeiro acesso depois de um período parado pode levar até 1 minuto.

## Funcionalidades

**Cliente:** vitrine com busca e filtro por categoria · carrinho salvo no navegador (24 h) · cadastro, login com bloqueio após 5 tentativas e recuperação de senha · endereços com preenchimento pelo CEP (ViaCEP) · frete grátis a partir de R$ 80 · pagamento com cartão (validação de Luhn e bandeira) ou PIX com QR Code · acompanhamento do pedido com atualização automática.

**Administrador:** cadastro, edição e desativação de produtos · gestão de pedidos com filtros, máquina de estados de status, cancelamento com estorno e devolução de estoque · comprovante para impressão.

## Stack

| Camada | Tecnologia |
|---|---|
| Front-end (View) | Ionic 9 + Angular 22 (standalone, signals) |
| Back-end (Controller / Model) | Java 17 + Spring Boot 4 (arquitetura MVC), Spring Security com JWT |
| Banco de dados | PostgreSQL 16 + Flyway (migrations versionadas) |
| Testes | JUnit 5, MockMvc, AssertJ (164 testes no back-end) · Jasmine/Karma |
| Hospedagem | Render: API em Docker, site estático e PostgreSQL gerenciado |

## Estrutura do repositório

```
cupcakes-app/
├── backend/          API REST em Spring Boot
├── frontend/         App Ionic/Angular
├── docs/
│   ├── pit-1/        Requisitos ágeis (User Stories + backlog)
│   ├── situacao-1/   Revisão do planejamento: UML, banco de dados, IHC
│   ├── situacao-2/   Manual de uso
│   └── situacao-3/   Testes com usuários, laudo de qualidade e correções
├── docker-compose.yml
└── .env.example
```

## Como rodar localmente

**Pré-requisitos:** Java 17, Node.js 20+ e Docker.

1. Configure as variáveis de ambiente (o arquivo `.env` não vai para o Git):
   ```bash
   cp .env.example .env
   ```
   Preencha a senha do banco, o `JWT_SECRET` (32 ou mais caracteres aleatórios) e o e-mail e a senha do administrador.

2. Suba o banco de dados:
   ```bash
   docker compose up -d
   ```

3. Rode a API em http://localhost:8080. Na primeira execução, o Flyway cria as tabelas e o catálogo inicial, e o administrador do `.env` é criado:
   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```

4. Em outro terminal, rode o front (o proxy encaminha `/api` para a API):
   ```bash
   cd frontend
   npm install
   npm start
   ```
   O app abre em http://localhost:4200. Com o Ionic CLI instalado, `ionic serve` abre em http://localhost:8100, a porta usada por padrão nos links de redefinição de senha (`FRONTEND_URL`).

**Testes do back-end** (usam H2 em memória, não precisam do Docker):
```bash
cd backend
./mvnw test
```

## Implantação (Render)

| Serviço | Configuração |
|---|---|
| PostgreSQL | Banco gerenciado, na mesma região da API |
| API | Web Service com Docker (`backend/Dockerfile`), health check em `/api/health` |
| Front | Static Site com rewrite de `/api/*` para a API e de `/*` para `/index.html` |

Variáveis da API: `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `JWT_SECRET`, `ADMIN_EMAIL`, `ADMIN_SENHA`, `TZ=America/Sao_Paulo` e `FRONTEND_URL` (base dos links de redefinição de senha).

## Autor

**Erick dos Santos Pereira** · Engenharia de Software

Desenvolvido com apoio do Claude (IA da Anthropic) para mentoria e revisão de código. Commits com participação da IA estão marcados com `Co-Authored-By`.

## Créditos
- Ícone de cupcake: [Streamline](https://www.streamlinehq.com) (licença CC BY 4.0)
