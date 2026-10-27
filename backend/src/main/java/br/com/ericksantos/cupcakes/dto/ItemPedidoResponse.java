package br.com.ericksantos.cupcakes.dto;

import br.com.ericksantos.cupcakes.model.ItemPedido;

import java.math.BigDecimal;

public record ItemPedidoResponse(Long produtoId, String produtoNome, int quantidade,
                                 BigDecimal precoUnitario, BigDecimal subtotal) {

    public static ItemPedidoResponse de(ItemPedido item) {
        BigDecimal subtotal = item.getPrecoUnitario().multiply(BigDecimal.valueOf(item.getQuantidade()));
        return new ItemPedidoResponse(item.getProduto().getId(), item.getProduto().getNome(),
            item.getQuantidade(), item.getPrecoUnitario(), subtotal);
    }
}
