package br.com.ericksantos.cupcakes.model.enums;

import java.util.Set;

public enum StatusPedido {
    AGUARDANDO_PAGAMENTO,
    PAGO,
    EM_PREPARO,
    SAIU_PARA_ENTREGA,
    ENTREGUE,
    CANCELADO;


    public boolean podeIrPara(StatusPedido destino){
        Set<StatusPedido> permitidos = switch (this){
            case AGUARDANDO_PAGAMENTO -> Set.of(PAGO,CANCELADO);
            case PAGO -> Set.of(EM_PREPARO,CANCELADO);
            case EM_PREPARO -> Set.of(SAIU_PARA_ENTREGA,CANCELADO);
            case SAIU_PARA_ENTREGA -> Set.of(ENTREGUE);
            case ENTREGUE,CANCELADO -> Set.of();
        };
        return permitidos.contains(destino);
    }

}
