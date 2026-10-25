package br.com.ericksantos.cupcakes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import br.com.ericksantos.cupcakes.dto.FreteResponse;
import br.com.ericksantos.cupcakes.exception.RegraNegocioException;

/** Teste unitário puro: sem Spring, sem banco, roda em milissegundos */
class FreteServiceTest {

    private final FreteService service = new FreteService();

    // RN#1, casos de borda: 80,00 exato já é grátis (decisão documentada)
    @ParameterizedTest
    @CsvSource({ "79.99, false", "80.00, true", "80.01, true" })
    void freteGratisAPartirDe80(String subtotal, boolean gratis) {
        FreteResponse r = service.calcular("01310100", new BigDecimal(subtotal));
        assertThat(r.gratis()).isEqualTo(gratis);
    }

    // CA#1 e CA#2: valor e prazo pelo 1º dígito do CEP
    @ParameterizedTest
    @CsvSource({
        "01310100, 8.00, 1",    // SP
        "13010000, 8.00, 1",    // interior de SP
        "20040002, 12.00, 2",   // RJ
        "30130000, 12.00, 2",   // MG
        "70040010, 18.00, 4",   // DF
        "90010000, 18.00, 4"    // RS
    })
    void valorEPrazoPorRegiao(String cep, String valor, int prazo) {
        FreteResponse r = service.calcular(cep, new BigDecimal("50.00"));
        assertThat(r.valor()).isEqualByComparingTo(valor);
        assertThat(r.prazoDias()).isEqualTo(prazo);
    }

    @Test
    void freteGratisZeraValorMasMantemPrazo() {
        FreteResponse r = service.calcular("90010000", new BigDecimal("100.00"));
        assertThat(r.valor()).isEqualByComparingTo("0");
        assertThat(r.prazoDias()).isEqualTo(4);
    }

    @Test
    void aceitaCepComTraco() {
        FreteResponse r = service.calcular("01310-100", new BigDecimal("50.00"));
        assertThat(r.prazoDias()).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(strings = { "123", "abc", "", "123456789" })
    void cepInvalidoDeveLancarExcecao(String cep) {
        assertThatThrownBy(() -> service.calcular(cep, new BigDecimal("50.00")))
            .isInstanceOf(RegraNegocioException.class)
            .hasMessage("CEP inválido.");
    }

    @Test
    void subtotalNegativoDeveLancarExcecao() {
        assertThatThrownBy(() -> service.calcular("01310100", new BigDecimal("-1")))
            .isInstanceOf(RegraNegocioException.class);
    }
}
