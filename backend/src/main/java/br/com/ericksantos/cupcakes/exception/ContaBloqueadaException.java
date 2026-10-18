package br.com.ericksantos.cupcakes.exception;

public class ContaBloqueadaException extends RuntimeException {
    public ContaBloqueadaException() { super("Muitas tentativas. Tente novamente em 15 minutos."); }
}
