package br.com.ericksantos.cupcakes.dto;

import br.com.ericksantos.cupcakes.model.Pedido;
import br.com.ericksantos.cupcakes.model.enums.StatusPedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponse(Long id, String numero, StatusPedido status,
                             BigDecimal subtotal, BigDecimal frete, BigDecimal total, LocalDateTime criadoEm,
                             String entregaLogradouro, String entregaNumero, String entregaComplemento,
                             String entregaBairro, String entregaCidade, String entregaUf, String entregaCep,
                             List<ItemPedidoResponse> itens) {

    public static PedidoResponse de(Pedido p) {
        return new PedidoResponse(p.getId(), p.getNumero(), p.getStatus(),
            p.getSubtotal(), p.getFrete(), p.getTotal(), p.getCriadoEm(),
            p.getEntregaLogradouro(), p.getEntregaNumero(), p.getEntregaComplemento(),
            p.getEntregaBairro(), p.getEntregaCidade(), p.getEntregaUf(), p.getEntregaCep(),
            p.getItens().stream().map(ItemPedidoResponse::de).toList());
    }
}
