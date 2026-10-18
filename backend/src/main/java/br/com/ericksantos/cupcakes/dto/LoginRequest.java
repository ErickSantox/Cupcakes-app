package br.com.ericksantos.cupcakes.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
    @NotBlank(message = "Informe o e-mail.")
    @Email(message = "Informe um e-mail válido.")
    String email,

    @NotBlank(message = "Informe a senha.")
    String senha
) {}
