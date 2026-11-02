package br.com.ericksantos.cupcakes.dto;

import br.com.ericksantos.cupcakes.model.enums.StatusPedido;
import jakarta.validation.constraints.NotNull;

public record AtualizarStatusRequest(@NotNull StatusPedido status) {
}
