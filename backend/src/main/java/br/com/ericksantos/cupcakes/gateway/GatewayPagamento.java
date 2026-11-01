package br.com.ericksantos.cupcakes.gateway;

import br.com.ericksantos.cupcakes.model.enums.FormaPagamento;

import java.math.BigDecimal;

public interface GatewayPagamento {
    ResultadoGateway autorizar(String numeroCartao, String cvv, BigDecimal valor, FormaPagamento forma);
}
