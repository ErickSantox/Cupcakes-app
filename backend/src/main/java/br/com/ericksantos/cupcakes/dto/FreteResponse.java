package br.com.ericksantos.cupcakes.dto;

import java.math.BigDecimal;

public record FreteResponse(BigDecimal valor, int prazoDias, boolean gratis) {

    public static FreteResponse de(BigDecimal valor, int prazoDias, boolean gratis){
        return new FreteResponse(valor, prazoDias, gratis);
    }
}
