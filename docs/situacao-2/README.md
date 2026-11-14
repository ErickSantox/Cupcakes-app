# Situação 2 — Manual de uso

**Endereço:** https://cupcakes-web.onrender.com

O app funciona no navegador do computador e do celular. Os pagamentos são **simulados**: nenhum valor é cobrado e nenhum dado de cartão é gravado.

> O servidor gratuito pode levar até 1 minuto para responder no primeiro acesso do dia. Se a vitrine não carregar, aguarde e recarregue a página.

## Para o cliente

### 1. Escolher os cupcakes
1. Na aba **Vitrine**, busque pelo nome ou sabor, ou filtre por categoria (Chocolate, Clássicos, Especiais, Frutas).
2. Toque em um cupcake para ver a descrição, o preço e o estoque disponível.
3. Escolha a quantidade e toque em **Adicionar**. O app volta para a vitrine e confirma a inclusão.

Produtos sem estoque aparecem com o selo **Esgotado** e não podem ser adicionados.

### 2. Revisar o carrinho
Na aba **Carrinho**, altere as quantidades com **−** e **+**, ou remova um item (é possível desfazer). O limite é o estoque disponível, até 50 unidades por sabor. O carrinho fica salvo no navegador por 24 horas, mesmo sem login.

### 3. Criar conta e entrar
Para finalizar a compra é preciso ter uma conta.
- **Criar conta:** na aba **Conta**, toque em **Criar conta** e informe nome, sobrenome, telefone com DDD, e-mail e uma senha de no mínimo 8 caracteres.
- **Entrar:** informe e-mail e senha. Depois de 5 tentativas erradas, a conta fica bloqueada por 15 minutos.
- **Esqueci minha senha:** na tela de login, informe o e-mail para receber um link de redefinição, válido por 15 minutos e de uso único. Nesta versão, o envio do e-mail é simulado: o link fica registrado no log do servidor.

### 4. Cadastrar endereço
Em **Conta → Meus endereços**, toque em **Novo endereço** e informe o CEP: rua, bairro, cidade e UF são preenchidos automaticamente. Se o CEP não for encontrado, preencha o endereço manualmente. Marque um endereço como **padrão** para ele já vir selecionado no checkout.

### 5. Finalizar o pedido
No carrinho, toque em **Finalizar pedido**, escolha o endereço e confira o frete (grátis para compras a partir de R$ 80,00). Toque em **Confirmar pedido**.

### 6. Pagar
| Forma | Como testar |
|---|---|
| Cartão de crédito ou débito aprovado | `4111 1111 1111 1111` · validade futura (ex.: `12/30`) · CVV `123` · qualquer nome |
| Cartão recusado (de propósito) | `4000 0000 0000 0002` |
| PIX | Escolha **PIX**, toque em **Gerar código PIX**, copie o código ou leia o QR Code (válido por 30 minutos) e toque em **Simular pagamento no banco** |

### 7. Acompanhar as compras
Na aba **Minhas compras**, toque em um pedido para ver a linha do tempo: Aguardando pagamento → Pago → Em preparo → Saiu para entrega → Entregue. Com a tela aberta, o status é atualizado sozinho a cada 10 segundos, com um aviso quando muda.

## Para o administrador

O acesso de administrador é criado na implantação, pelas variáveis `ADMIN_EMAIL` e `ADMIN_SENHA`. Depois do login, a tela **Conta** mostra os botões da área administrativa.

### Gerenciar produtos
Em **Área do administrador**:
- **Novo produto:** nome, descrição, preço (até R$ 9.999,99), categoria, foto e estoque (até 99.999).
- **Editar:** altere qualquer campo, inclusive o estoque.
- **Ativar/desativar:** produtos desativados somem da vitrine, mas continuam no histórico de pedidos.

### Gerenciar pedidos
Em **Gerenciar pedidos**:
- **Filtrar** por status e por período (de/até).
- **Avançar o status** com o botão "Marcar como…": Pago → Em preparo → Saiu para entrega → Entregue. O status Pago só vem do pagamento.
- **Cancelar** pedidos aguardando pagamento, pagos ou em preparo. O estoque volta e o pagamento é estornado (simulado). Um pedido que já saiu para entrega não pode ser cancelado.
- **Comprovante:** abre o comprovante do pedido e o botão **Imprimir**.
