package br.com.ericksantos.cupcakes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import br.com.ericksantos.cupcakes.exception.RegraNegocioException;
import br.com.ericksantos.cupcakes.model.enums.Bandeira;

/** Teste unitário puro: sem Spring, sem banco, roda em milissegundos */
class ValidadorCartaoTest {

    private final ValidadorCartao validador = new ValidadorCartao();

    private static final String VISA = "4111111111111111";
    private static final String CVV = "123";

    private static final DateTimeFormatter MM_AA = DateTimeFormatter.ofPattern("MM/yy");

    /** Validades relativas a hoje: o teste não quebra com o passar do tempo */
    private static String validadeDaqui(int meses) {
        return YearMonth.now().plusMonths(meses).format(MM_AA);
    }

    private static final String VALIDADE_OK = validadeDaqui(12);

    // ---------- RN#1: bandeiras aceitas (números de teste públicos, todos passam no Luhn) ----------

    @ParameterizedTest
    @CsvSource({
        "4111111111111111, VISA",
        "4222222222222,    VISA",         // Visa com 13 dígitos
        "5555555555554444, MASTERCARD",
        "5105105105105100, MASTERCARD",
        "2223000048400011, MASTERCARD",   // faixa 2221–2720
        "6362970000457013, ELO",
        "4389350000000002, ELO",          // Elo que começa com 4: não pode virar Visa
    })
    void identificaABandeiraPeloBin(String numero, Bandeira esperada) {
        assertThat(validador.validar(numero, VALIDADE_OK, CVV)).isEqualTo(esperada);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "6011111111111117",   // Discover
        "378282246310005",    // American Express
    })
    void bandeiraNaoAceitaDeveSerRecusada(String numero) {
        assertThatThrownBy(() -> validador.validar(numero, VALIDADE_OK, CVV))
            .isInstanceOf(RegraNegocioException.class)
            .hasMessage("Bandeira não aceita. Aceitamos Visa, Mastercard e Elo.");
    }

    // ---------- número: normalização, tamanho e Luhn ----------

    @ParameterizedTest
    @ValueSource(strings = { "4111 1111 1111 1111", "4111-1111-1111-1111", " 4111111111111111 " })
    void aceitaNumeroComEspacosOuTracos(String numero) {
        assertThat(validador.validar(numero, VALIDADE_OK, CVV)).isEqualTo(Bandeira.VISA);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "4111111111111112",       // falha no Luhn (último dígito trocado)
        "5555555555554445",       // falha no Luhn
        "4111",                   // curto demais (e não pode estourar no substring)
        "41111111111111111111",   // 20 dígitos
        "abcd efgh ijkl mnop",    // sem nenhum dígito
    })
    void numeroInvalidoDeveSerRecusado(String numero) {
        assertThatThrownBy(() -> validador.validar(numero, VALIDADE_OK, CVV))
            .isInstanceOf(RegraNegocioException.class)
            .hasMessage("Número de cartão inválido.");
    }

    // ---------- validade ----------

    @Test
    void cartaoQueVenceEsteMesAindaEValido() {
        assertThat(validador.validar(VISA, validadeDaqui(0), CVV)).isEqualTo(Bandeira.VISA);
    }

    @Test
    void cartaoQueVenceuMesPassadoDeveSerRecusado() {
        assertThatThrownBy(() -> validador.validar(VISA, validadeDaqui(-1), CVV))
            .isInstanceOf(RegraNegocioException.class)
            .hasMessage("Cartão vencido.");
    }

    @ParameterizedTest
    @ValueSource(strings = { "1027", "1/27", "10/2027", "ab/cd", "" })
    void validadeForaDoFormatoDeveSerRecusada(String validade) {
        assertThatThrownBy(() -> validador.validar(VISA, validade, CVV))
            .isInstanceOf(RegraNegocioException.class)
            .hasMessage("Validade inválida. Use MM/AA.");
    }

    @ParameterizedTest
    @ValueSource(strings = { "00/30", "13/30", "10/98" })
    void validadeImpossivelDeveSerRecusada(String validade) {
        assertThatThrownBy(() -> validador.validar(VISA, validade, CVV))
            .isInstanceOf(RegraNegocioException.class)
            .hasMessage("Validade inválida.");
    }

    // ---------- CVV ----------

    @ParameterizedTest
    @ValueSource(strings = { "12", "1234", "abc", "" })
    void cvvInvalidoDeveSerRecusado(String cvv) {
        assertThatThrownBy(() -> validador.validar(VISA, VALIDADE_OK, cvv))
            .isInstanceOf(RegraNegocioException.class)
            .hasMessage("CVV inválido.");
    }
}
