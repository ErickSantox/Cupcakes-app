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
- Não havia história para criar o administrador: ele é criado pela carga inicial do banco (migration).

## 5. Correções na documentação do PIT I
- A tabela de tempo estava com os valores deslocados uma linha.
- A tabela do backlog (Apêndice B) estava com as linhas desalinhadas.
