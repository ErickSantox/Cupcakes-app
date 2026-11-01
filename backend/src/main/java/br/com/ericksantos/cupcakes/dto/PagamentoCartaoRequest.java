package br.com.ericksantos.cupcakes.dto;

import br.com.ericksantos.cupcakes.model.enums.FormaPagamento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PagamentoCartaoRequest(
    @NotBlank(message = "Informe o número do cartão.") String numero,

    @NotBlank(message = "Informe o nome impresso no cartão.") String nomeTitular,

    @NotBlank(message = "Informe a validade.") String validade,

    @NotBlank(message = "Informe o CVV.") String cvv,

    @NotNull(message = "Informe crédito ou débito.") FormaPagamento forma
) {

    /** CA#3: número e CVV nunca aparecem em log; só os 4 últimos dígitos */
    @Override
    public String toString() {
        String numeroMascarado = (numero == null || numero.length() < 4)
            ? "****"
            : "*".repeat(numero.length() - 4) + numero.substring(numero.length() - 4);

        return "PagamentoCartaoRequest[numero=" + numeroMascarado + ", cvv=***, forma=" + forma + "]";
    }
}
