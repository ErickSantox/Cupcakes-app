package br.com.ericksantos.cupcakes.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ProdutoRequest(
    @NotBlank(message = "Informe o nome.")
    @Size(max = 100) String nome,

    @NotBlank(message = "Informe a descrição")
    @Size(max = 200) String descricao,

    @NotNull(message = "Informe o preço")
    @Positive(message = "O preço deve ser maior que zero")
    @DecimalMax(value = "9999.99", message = "O preço máximo é R$ 9.999,99.") BigDecimal preco,

    @NotNull(message = "Escolha a categoria.") Long categoriaId,

    @NotBlank(message = "A foto é obrigatória.") @Size(max = 255) String fotoUrl,

    @NotNull(message = "Informe o estoque.")
    @PositiveOrZero(message = "O estoque não pode ser negativo.")
    @Max(value = 99999, message = "O estoque máximo é 99.999 unidades.") Integer estoque


    ) {
}
