package br.com.ericksantos.cupcakes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EnderecoRequest(
    @NotBlank(message = "Informe o CEP.")
    @Pattern(regexp = "\\d{8}", message = "O CEP deve ter 8 números.") String cep,

    @NotBlank(message = "Informe a rua.") @Size(max = 100) String logradouro,

    @NotBlank(message = "Informe o número.") @Size(max = 10) String numero,

    @Size(max = 50) String complemento,

    @NotBlank(message = "Informe o bairro.") @Size(max = 50) String bairro,

    @NotBlank(message = "Informe a cidade.") @Size(max = 50) String cidade,

    @NotBlank(message = "Informe a UF.")
    @Pattern(regexp = "[A-Z]{2}", message = "UF inválida.") String uf,

    boolean padrao
) {}
