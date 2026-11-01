package br.com.ericksantos.cupcakes.gateway;

/** Resposta do gateway: aprovado ou recusado (com o motivo) */
public record ResultadoGateway(boolean aprovado, String motivo) {

    public static ResultadoGateway aprovacao() {
        return new ResultadoGateway(true, null);
    }

    public static ResultadoGateway recusa(String motivo) {
        return new ResultadoGateway(false, motivo);
    }
}
