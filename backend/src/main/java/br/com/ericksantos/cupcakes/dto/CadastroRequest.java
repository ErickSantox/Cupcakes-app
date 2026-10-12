package br.com.ericksantos.cupcakes.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CadastroRequest(
    @NotBlank(message = "Informe o nome.")
    @Size(min = 2,max = 100, message = "O nome deve ter no mínimo 2 caracteres.")
    String nome,
    @NotBlank(message = "Informe o sobrenome.")
    @Size(min = 2,max = 100, message = "O sobrenome deve ter no mínimo 2 caracteres.")
    String sobrenome,
    @NotBlank(message = "Informe o telefone.")
    @Pattern(regexp = "\\d{10,11}", message = "Informe DDD + número, somente dígitos. Ex.: 11999998888")
    String telefone,
    @NotBlank(message = "Informe um e-mail.")
    @Email(message = "Por favor, insira um e-mail válido.")
    @Size(max = 150, message = "O email deve ter no máximo 150 caracteres.")
    String email,
    @NotBlank(message = "Informe uma senha.")
    @Size(min = 8, max = 72, message = "A senha deve ter no mínimo 8 caracteres.")
    String senha) {
}
