package br.com.ericksantos.cupcakes.dto;

import jakarta.validation.constraints.NotNull;

public record AtivoRequest(@NotNull Boolean ativo) {
}
