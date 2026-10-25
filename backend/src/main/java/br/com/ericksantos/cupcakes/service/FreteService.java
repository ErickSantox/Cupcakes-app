package br.com.ericksantos.cupcakes.service;

import br.com.ericksantos.cupcakes.dto.FreteResponse;
import br.com.ericksantos.cupcakes.exception.RegraNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class FreteService {

    static final BigDecimal LIMITE_GRATIS = new BigDecimal("80.00");

    /** Tabela simulada pelo 1º dígito do CEP */
    private enum Regiao {
        SP(new BigDecimal("8.00"), 1),
        SUDESTE(new BigDecimal("12.00"), 2),
        DEMAIS(new BigDecimal("18.00"), 4);

        private final BigDecimal valor;
        private final int prazoDias;

        Regiao(BigDecimal valor, int prazoDias) {
            this.valor = valor;
            this.prazoDias = prazoDias;
        }

        static Regiao doDigito(int digito) {
            if (digito <= 1) return SP;
            if (digito <= 3) return SUDESTE;
            return DEMAIS;
        }
    }

    public FreteResponse calcular(String cep, BigDecimal subtotal) {
        String cepNormalizado = cep.replaceAll("\\D", "");
        if (cepNormalizado.length() != 8) {
            throw new RegraNegocioException("CEP inválido.");
        }
        if (subtotal.signum() < 0) {
            throw new RegraNegocioException("Subtotal inválido.");
        }

        Regiao regiao = Regiao.doDigito(cepNormalizado.charAt(0) - '0');
        boolean gratis = subtotal.compareTo(LIMITE_GRATIS) >= 0;
        BigDecimal valor = gratis ? BigDecimal.ZERO : regiao.valor;

        return new FreteResponse(valor, regiao.prazoDias, gratis);
    }
}
