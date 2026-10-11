package br.com.ericksantos.cupcakes.exception;


import br.com.ericksantos.cupcakes.dto.ErroResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErroResponse validacao(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
            .forEach(e -> campos.putIfAbsent(e.getField(), e.getDefaultMessage()));
        return new ErroResponse(400, "Validação", "Verifique os campos informados.",
            campos, LocalDateTime.now());
    }

    @ExceptionHandler(RegraNegocioException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ErroResponse regraNegocio(RegraNegocioException ex) {
        return ErroResponse.de(422, "Regra de negócio", ex.getMessage());
    }

    @ExceptionHandler(NaoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErroResponse naoEncontrado(NaoEncontradoException ex) {
        return ErroResponse.de(404, "Não encontrado", ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErroResponse integridade(DataIntegrityViolationException ex) {
        log.warn("Violação de integridade: {}", ex.getMostSpecificCause().getMessage());
        return ErroResponse.de(409, "Conflito", "Os dados violam uma regra do sistema.");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErroResponse inesperado(Exception ex) {
        log.error("Erro inesperado", ex);
        return ErroResponse.de(500, "Erro interno", "Ocorreu um erro inesperado. Tente novamente.");
    }
}
