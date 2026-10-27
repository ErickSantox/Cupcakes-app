package br.com.ericksantos.cupcakes.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PedidoRequest(
    @NotNull(message = "Selecione um endereço de entrega.") Long enderecoId,

    @NotEmpty(message = "O pedido precisa ter pelo menos um item.")
    @Valid List<ItemPedidoRequest> itens
) {}
