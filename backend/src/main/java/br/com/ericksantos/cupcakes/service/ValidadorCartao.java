package br.com.ericksantos.cupcakes.service;

import br.com.ericksantos.cupcakes.exception.RegraNegocioException;
import br.com.ericksantos.cupcakes.model.enums.Bandeira;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.List;

@Component
public class ValidadorCartao {

    /** Lista PARCIAL de BINs Elo (suficiente para o gateway simulado) */
    private static final List<String> BINS_ELO = List.of(
        "401178", "401179", "431274", "438935", "451416", "457393",
        "457631", "457632", "504175", "627780", "636297", "636368");

    private boolean passaNoLuhn(String numero) {
        int soma = 0;
        boolean dobrar = false;

        for (int i = numero.length() - 1; i >= 0; i--) {
            int digito = numero.charAt(i) - '0';

            if(dobrar) {
                digito = digito * 2;
                if(digito > 9) {
                    digito = digito - 9;
                }
            }
            soma += digito;
            dobrar = !dobrar;
        }
        return soma % 10 == 0;
    }

    private Bandeira identificarBandeira(String numero){
        int prefixo4 = Integer.parseInt(numero.substring(0, 4));
        int prefixo2 = Integer.parseInt(numero.substring(0, 2));

        if(BINS_ELO.stream().anyMatch(numero::startsWith)) {
            return Bandeira.ELO;
        } else if ((prefixo4 >= 2221 && prefixo4 <= 2720) || (prefixo2 >= 51 && prefixo2 <= 55) ) {
            return Bandeira.MASTERCARD;
        } else if (numero.startsWith("4")) {
            return Bandeira.VISA;
        }else{
            throw new RegraNegocioException("Bandeira não aceita. Aceitamos Visa, Mastercard e Elo.");
        }
    }

    private void validarValidade(String validade){
        if(!validade.matches("\\d{2}/\\d{2}")) {
            throw new RegraNegocioException("Validade inválida. Use MM/AA.");
        }

        String[] partes = validade.split("/");
        int mes = Integer.parseInt(partes[0]);
        int ano = Integer.parseInt(partes[1]) + 2000;

        if (mes < 1 || mes > 12) {
            throw new RegraNegocioException("Validade inválida.");
        }

        if (YearMonth.of(ano, mes).isBefore(YearMonth.now())) {
            throw new RegraNegocioException("Cartão vencido.");
        }

        if (YearMonth.of(ano, mes).isAfter(YearMonth.now().plusYears(20))) {
            throw new RegraNegocioException("Validade inválida.");
        }
    }

    private void validarCvv(String cvv){
        if(!cvv.matches("\\d{3}")) {
            throw new RegraNegocioException("CVV inválido.");
        }
    }

    public Bandeira validar(String numero, String validade, String cvv){
        String digitos = numero.replaceAll("\\D", "");
        if(digitos.length() < 13 || digitos.length() > 19) {
            throw new RegraNegocioException("Número de cartão inválido.");
        }

        if (!passaNoLuhn(digitos)) { throw new RegraNegocioException("Número de cartão inválido.");}

        Bandeira bandeira = identificarBandeira(digitos);
        validarValidade(validade);
        validarCvv(cvv);
        return bandeira;
    }
}
