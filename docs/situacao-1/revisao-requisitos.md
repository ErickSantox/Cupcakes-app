# Revisão dos Requisitos (Kaizen) — PIT II / Situação 1

Revisão das 15 User Stories do PIT I antes da implementação, buscando
ambiguidades, conflitos e itens inviáveis no escopo acadêmico.

## 1. Ambiguidades resolvidas
| US | Problema encontrado | Decisão |
|----|--------------------|---------|
| US-02 | RN#1 bloqueia a conta após 5 tentativas, mas não diz como desbloquear | Bloqueio temporário de 15 min |
| US-12 | "Frete grátis acima de R$ 80" — R$ 80,00 exatos ganha? | Grátis para subtotal >= R$ 80,00 |
| US-13 x US-15 | US-13 lista 3 status; o fluxo real precisa de pagamento e cancelamento | Status unificados: AGUARDANDO_PAGAMENTO → PAGO → EM_PREPARO → SAIU_PARA_ENTREGA → ENTREGUE (+ CANCELADO) |

## 2. Conflitos entre histórias
| US            | Conflito | Decisão                                                                                                   |
|---------------|----------|-----------------------------------------------------------------------------------------------------------|
| US-06 x US-08 | Carrinho persiste "sem login", mas finalizar exige conta | Carrinho salvo no app (24h); login exigido só no checkout

## 3. Adequações de escopo (simulações)
| US | Requisito original                     | Implementação no PIT II | Motivo |
|----|----------------------------------------|------------------------|--------|
| US-09 | Integração com gateway                 | Gateway simulado (Luhn + bandeira) | Gateway real exige contrato comercial |
| US-10 | API do Banco Central                   | QR Code gerado localmente + botão "simular pagamento" | API PIX exige certificado de instituição financeira |
| US-11 | API dos Correios                       | ViaCEP (API pública) | API dos Correios exige contrato |
| US-13 | Notificação push                       | Atualização automática (polling) + aviso na tela | Push exige Firebase/publicação em loja |
| US-01/08 | Envio de e-mail                        | Registro em log | Sem servidor SMTP no escopo |
 | US-02(CA#3) | Recuperação de senha (Link por e-mail) |Token de redefinição (15 min, uso único) com o link registrado em log | sem servidor SMTP no escopo; o fluxo de segurança é mantido|   

## 4. Lacunas identificadas
- US-14 cobre só o cadastro: incluídas edição e desativação de produto (desativar em vez de excluir, para preservar o histórico de pedidos).
- Não havia história para criar o administrador: ele é criado na inicialização da aplicação (`AdminSeeder`), a partir das variáveis de ambiente `ADMIN_EMAIL` e `ADMIN_SENHA`, para que nenhuma senha fique gravada no repositório.

## 5. Correções na documentação do PIT I
- A tabela de tempo estava com os valores deslocados uma linha.
- A tabela do backlog (Apêndice B) estava com as linhas desalinhadas.

## 6. Atualizações durante o desenvolvimento (PIT II)
Decisões tomadas ao implementar as histórias, que complementam esta revisão.

| História | Atualização | Motivo |
|---|---|---|
| US-15 | O admin não pode marcar um pedido como PAGO; esse status só vem do pagamento (US-09/10). As transições seguem uma máquina de estados (`StatusPedido.podeIrPara`), e transições inválidas retornam 422 | Evitar que um pedido não pago seja preparado ou entregue |
| US-15 | Cancelar um pedido devolve o estoque e estorna o pagamento de forma simulada (novo status de pagamento ESTORNADO, migration V4). Pedido que já saiu para entrega não pode ser cancelado | Manter estoque e pagamento coerentes com o pedido |
| US-15 CA#3 | Comprovante impresso a partir de uma página própria, isolada do layout do app | O layout do Ionic não imprime corretamente |
| US-13 | O status é atualizado a cada 10 s apenas enquanto a tela do pedido está aberta, com aviso na tela quando muda | Alternativa ao push (ver item 3), sem gastar requisições em segundo plano |
| US-06 | O carrinho guarda o estoque de cada produto e limita a quantidade a ele, além do limite de 50 | Achado nos testes com usuários (Situação 3) |
| US-09 | O número do cartão também é validado no front (Luhn, 16 dígitos) | Achado nos testes com usuários (Situação 3) |
| US-13 | A aba "Pedidos" passou a se chamar "Minhas compras", com atalhos na Conta e após o pagamento | Um testador não encontrou a área (Situação 3) |
| — | Tema escuro (dark mode) registrado para uma versão futura | Sugestão de testador fora do escopo desta entrega |

O relatório completo dos testes e das correções está na [Situação 3](../situacao-3/README.md).
