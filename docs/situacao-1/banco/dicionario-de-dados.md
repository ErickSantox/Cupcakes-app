# Dicionário de Dados - Cupcakes App

SGBD: PosgtreSQL 16 · Convenções: nomes em snake_case, sem acento;
dinheiro em DECIMAL(10,2); datas em TIMESTAMP; enums gravados como texto.

## usuario
Pessoas que acessam o sistema (clientes e administradores).

| Campo | Tipo | Obrig. | Chave | Regra / Domínio                                                             | Descrição                       |
|---|---|-----|---|-----------------------------------------------------------------------------|---------------------------------|
| id | BIGSERIAL | Sim | PK | Gerado automaticamente                                                      | Identificador do usuário        |
| nome | VARCHAR(100) | Sim | | Mín. 2 caracteres; obrigatório (US-01 RN#2)                                 | Nome do Usuário                 |
| sobrenome | VARCHAR(100) | Sim | | Válido de no mínimo 2 caracteres                                            | Sobrenome do Usuário            |
| telefone | VARCHAR(11) | Sim | | Apenas dígitos; 10 (fixo) ou 11 (celular) com DDD; obrigatório (US-01 RN#2) | DDD + Telefone do Usuário | 
| email | VARCHAR(150) | Sim | UNIQUE | Formato de e-mail válido; não pode repetir (US-01 RN#1)                     | Login do usuário                |
| senha_hash | VARCHAR(100) | Sim | | Hash BCrypt; a senha pura nunca é gravada                                   | Senha criptografada             |
| perfil | VARCHAR(20) | Sim | | CLIENTE ou ADMIN; padrão CLIENTE                                            | Define o acesso ao painel admin |
| tentativas_falhas | INT | Sim | | >= 0; padrão 0; zera no login com sucesso (US-02 RN#1)                      | Contador de senhas erradas      |
| bloqueado_ate | TIMESTAMP | Não | | Preenchido ao atingir 5 falhas (+15 min)                                    | Fim do bloqueio temporário      |
| criado_em | TIMESTAMP | Sim | | Preenchido automaticamente na criação                                       | Data de cadastro |
| atualizado_em | TIMESTAMP | Sim | | Atualizado a cada alteração                                                 | Data da última alteração |


## endereco
Endereços de entrega cadastrados pelo cliente (US-11).

| Campo | Tipo | Obrig. | Chave | Regra / Domínio | Descrição |
|---|---|---|---|---|---|
| id | BIGSERIAL | Sim | PK | Gerado automaticamente | Identificador do endereço |
| usuario_id | BIGINT | Sim | FK → usuario.id | Máx. 5 endereços por usuário (US-11 RN#1) | Dono do endereço |
| logradouro | VARCHAR(100) | Sim | | Preenchido pelo ViaCEP, editável | Rua, avenida etc. |
| numero | VARCHAR(10) | Sim | | Texto para aceitar "S/N", "120A" | Número do imóvel |
| cep | CHAR(8) | Sim | | Apenas 8 dígitos, sem hífen; validado no ViaCEP (US-11 CA#3) | CEP |
| complemento | VARCHAR(50) | Não | | Opcional | Apto, bloco, referência |
| bairro | VARCHAR(50) | Sim | | Preenchido pelo ViaCEP | Bairro |
| cidade | VARCHAR(50) | Sim | | Preenchido pelo ViaCEP | Cidade |
| uf | VARCHAR(2) | Sim | | Sigla de UF válida (ex.: SP) | Estado |
| padrao | BOOLEAN | Sim | | Apenas 1 endereço padrão por usuário (US-11 CA#2) | Endereço sugerido no checkout |
| criado_em | TIMESTAMP | Sim | | Preenchido automaticamente na criação | Data de cadastro |
| atualizado_em | TIMESTAMP | Sim | | Atualizado a cada alteração | Data da última alteração |

## categoria
Agrupamento dos produtos na vitrine (US-04 CA#3).

| Campo | Tipo | Obrig. | Chave | Regra / Domínio | Descrição |
|---|---|---|---|---|---|
| id | BIGSERIAL | Sim | PK | Gerado automaticamente | Identificador da categoria |
| nome | VARCHAR(100) | Sim | UNIQUE | Não pode repetir | Nome exibido no filtro (ex.: Chocolate) |
| criado_em | TIMESTAMP | Sim | | Preenchido automaticamente na criação | Data de cadastro |
| atualizado_em | TIMESTAMP | Sim | | Atualizado a cada alteração | Data da última alteração |

## produto
Cupcakes do catálogo (US-04, US-14).

| Campo | Tipo | Obrig. | Chave | Regra / Domínio | Descrição |
|---|---|---|---|---|---|
| id | BIGSERIAL | Sim | PK | Gerado automaticamente | Identificador do produto |
| categoria_id | BIGINT | Sim | FK → categoria.id | Categoria existente | Categoria do produto |
| nome | VARCHAR(100) | Sim | | Buscável sem diferenciar maiúsculas/minúsculas (US-05 RN#1) | Nome do cupcake |
| descricao | VARCHAR(200) | Sim | | Exibida na vitrine (US-04 CA#1); usada na busca por sabor | Descrição/sabor |
| estoque | INT | Sim | | >= 0; quando 0, aparece como indisponível (US-04 CA#2) | Unidades disponíveis |
| preco | DECIMAL(10,2) | Sim | | > 0 (US-14 RN#1) | Preço atual de venda |
| foto_url | VARCHAR(255) | Sim | | Obrigatória no cadastro (US-14 RN#2) | Caminho da imagem |
| ativo | BOOLEAN | Sim | | Padrão true; só ativos aparecem na vitrine (US-04 RN#1); desativa em vez de excluir | Disponível no catálogo |
| criado_em | TIMESTAMP | Sim | | Preenchido automaticamente na criação | Data de cadastro |
| atualizado_em | TIMESTAMP | Sim | | Atualizado a cada alteração | Data da última alteração |

## pedido
Compra finalizada pelo cliente (US-08, US-13, US-15).

| Campo | Tipo | Obrig. | Chave | Regra / Domínio | Descrição |
|---|---|---|---|---|---|
| id | BIGSERIAL | Sim | PK | Gerado automaticamente | Identificador interno |
| usuario_id | BIGINT | Sim | FK → usuario.id | Cliente autenticado | Quem fez o pedido |
| numero | VARCHAR(20) | Sim | UNIQUE | Formato CPK-AAAA-NNNNNN (US-08 CA#3) | Número exibido ao cliente |
| subtotal | DECIMAL(10,2) | Sim | | Soma de quantidade × preco_unitario dos itens | Valor dos produtos |
| frete | DECIMAL(10,2) | Sim | | >= 0; 0 quando subtotal >= R$ 80,00 (US-12 RN#1) | Valor do frete |
| total | DECIMAL(10,2) | Sim | | subtotal + frete | Valor cobrado |
| status | VARCHAR(50) | Sim | | AGUARDANDO_PAGAMENTO, PAGO, EM_PREPARO, SAIU_PARA_ENTREGA, ENTREGUE, CANCELADO; só o ADMIN altera após o pagamento (US-15 RN#1) | Situação do pedido |
| entrega_logradouro | VARCHAR(100) | Sim | | Cópia do endereço no momento da compra | Rua da entrega |
| entrega_numero | VARCHAR(10) | Sim | | Cópia | Número da entrega |
| entrega_cep | CHAR(8) | Sim | | Cópia; base do cálculo do frete (US-12 CA#1) | CEP da entrega |
| entrega_complemento | VARCHAR(50) | Não | | Cópia | Complemento da entrega |
| entrega_bairro | VARCHAR(50) | Sim | | Cópia | Bairro da entrega |
| entrega_cidade | VARCHAR(50) | Sim | | Cópia | Cidade da entrega |
| entrega_uf | VARCHAR(2) | Sim | | Cópia | UF da entrega |
| criado_em | TIMESTAMP | Sim | | Preenchido automaticamente na criação | Data do pedido |
| atualizado_em | TIMESTAMP | Sim | | Atualizado a cada mudança de status | Data da última alteração |

## item_pedido
Produtos de cada pedido; resolve o N:N entre pedido e produto.

| Campo | Tipo | Obrig. | Chave | Regra / Domínio | Descrição |
|---|---|---|---|---|---|
| id | BIGSERIAL | Sim | PK | Gerado automaticamente | Identificador do item |
| pedido_id | BIGINT | Sim | FK → pedido.id | Todo pedido tem ao menos 1 item (US-08 RN#1) | Pedido ao qual pertence |
| produto_id | BIGINT | Sim | FK → produto.id | Produto ativo e com estoque na compra | Produto comprado |
| quantidade | INT | Sim | | Entre 1 e 50 (US-06 RN#1) | Unidades compradas |
| preco_unitario | DECIMAL(10,2) | Sim | | Preço do produto no momento da compra (congelado) | Valor pago por unidade |
| criado_em | TIMESTAMP | Sim | | Preenchido automaticamente; o item não é alterado depois | Data de inclusão |

## pagamento
Pagamento de um pedido (1:1) — cartão ou PIX simulados (US-09, US-10).

| Campo | Tipo | Obrig. | Chave | Regra / Domínio | Descrição |
|---|---|---|---|---|---|
| id | BIGSERIAL | Sim | PK | Gerado automaticamente | Identificador do pagamento |
| pedido_id | BIGINT | Sim | FK → pedido.id, UNIQUE | Um pagamento por pedido | Pedido pago |
| forma | VARCHAR(50) | Sim | | CARTAO_CREDITO, CARTAO_DEBITO ou PIX | Forma de pagamento |
| status | VARCHAR(50) | Sim | | PENDENTE, APROVADO, RECUSADO ou EXPIRADO | Situação do pagamento |
| valor | DECIMAL(10,2) | Sim | | Igual ao pedido.total | Valor cobrado |
| bandeira | VARCHAR(50) | Não | | Só se forma = CARTAO_*; VISA, MASTERCARD ou ELO (US-09 RN#1) | Bandeira do cartão |
| ultimos_digitos | CHAR(4) | Não | | Só se forma = CARTAO_*; número completo e CVV nunca são gravados (US-09 CA#3) | Exibido como **** 1234 |
| pix_copia_cola | VARCHAR(512) | Não | | Só se forma = PIX; gerado pelo sistema (simulado) | Código PIX copia-e-cola |
| pix_expira_em | TIMESTAMP | Não | | Só se forma = PIX; criado_em + 30 min (US-10 RN#1) | Validade do QR Code |
| criado_em | TIMESTAMP | Sim | | Preenchido automaticamente na criação | Data da tentativa |
| atualizado_em | TIMESTAMP | Sim | | Atualizado a cada mudança de status | Data da última alteração |

## token_redefinicao_senha
Tokens do fluxo "esqueci minha senha" (US-02 CA#3, revisão Kaizen).

| Campo | Tipo | Obrig. | Chave | Regra / Domínio | Descrição |
|---|---|---|---|---|---|
| id | BIGSERIAL | Sim | PK | Gerado automaticamente | Identificador do token |
| usuario_id | BIGINT | Sim | FK → usuario.id | Usuário que pediu a redefinição | Dono do token |
| token_hash | VARCHAR(100) | Sim | UNIQUE | Hash SHA-256; o token puro só existe no link | Token criptografado |
| criado_em | TIMESTAMP | Sim | | Preenchido automaticamente | Data da solicitação |
| expira_em | TIMESTAMP | Sim | | criado_em + 15 min | Validade do token |
| usado_em | TIMESTAMP | Não | | Nulo até o uso; token usado não vale de novo | Data em que foi utilizado |


