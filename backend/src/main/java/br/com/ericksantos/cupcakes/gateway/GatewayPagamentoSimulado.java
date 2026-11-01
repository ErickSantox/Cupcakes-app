package br.com.ericksantos.cupcakes.gateway;

import br.com.ericksantos.cupcakes.model.enums.FormaPagamento;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Gateway SIMULADO (não processa pagamentos reais).
 * Regra: cartões terminados em 0002 são recusados; os demais, aprovados.
 * Cartão para testar a recusa: 4000 0000 0000 0002.
 */
@Component
public class GatewayPagamentoSimulado implements GatewayPagamento {

    @Override
    public ResultadoGateway autorizar(String numeroCartao, String cvv, BigDecimal valor, FormaPagamento forma) {
        if (numeroCartao.endsWith("0002")) {
            return ResultadoGateway.recusa("Transação não autorizada pelo emissor.");
        }
        return ResultadoGateway.aprovacao();
    }
}
