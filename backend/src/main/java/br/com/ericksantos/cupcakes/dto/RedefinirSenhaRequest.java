package br.com.ericksantos.cupcakes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RedefinirSenhaRequest(
    @NotBlank(message = "Link de redefinição inválido.") String token,

    @NotBlank(message = "Informe a nova senha.")
    @Size(min = 8, max = 72, message = "A senha deve ter no mínimo 8 caracteres.") String novaSenha) {
}
