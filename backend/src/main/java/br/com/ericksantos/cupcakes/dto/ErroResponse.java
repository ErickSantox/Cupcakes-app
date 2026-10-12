package br.com.ericksantos.cupcakes.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record ErroResponse(int status, String erro, String mensagem, Map<String, String> campos, LocalDateTime timestamp) {

    public static ErroResponse de(int status, String erro, String mensagem) {
        return new ErroResponse(status,erro,mensagem,null,LocalDateTime.now());
    }
}
