package br.com.ericksantos.cupcakes.dto;

import br.com.ericksantos.cupcakes.model.Pagamento;
import br.com.ericksantos.cupcakes.model.enums.FormaPagamento;
import br.com.ericksantos.cupcakes.model.enums.StatusPagamento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PagamentoResponse(Long id, FormaPagamento forma, StatusPagamento status, BigDecimal valor,
                                String bandeira, String ultimosDigitos, String motivo, LocalDateTime criadoEm,
                                String pixCopiaCola, LocalDateTime pixExpiraEm) {

    /** O motivo vem do gateway (não é coluna no banco) */
    public static PagamentoResponse de(Pagamento p, String motivo) {
        return new PagamentoResponse(p.getId(), p.getForma(), p.getStatus(), p.getValor(),
            p.getBandeira(), p.getUltimosDigitos(), motivo, p.getCriadoEm(),
            p.getPixCopiaCola(), p.getPixExpiraEm());
    }
}
