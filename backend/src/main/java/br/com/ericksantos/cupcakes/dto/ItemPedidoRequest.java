package br.com.ericksantos.cupcakes.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ItemPedidoRequest(
    @NotNull(message = "Informe o produto.") Long produtoId,

    @NotNull(message = "Informe a quantidade.")
    @Min(value = 1, message = "A quantidade mínima por item é 1.")
    @Max(value = 50, message = "A quantidade máxima por item é 50.") Integer quantidade
) {}
