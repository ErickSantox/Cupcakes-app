package br.com.ericksantos.cupcakes.service;

import br.com.ericksantos.cupcakes.dto.PagamentoCartaoRequest;
import br.com.ericksantos.cupcakes.dto.PagamentoResponse;
import br.com.ericksantos.cupcakes.exception.NaoEncontradoException;
import br.com.ericksantos.cupcakes.exception.RegraNegocioException;
import br.com.ericksantos.cupcakes.gateway.GatewayPagamento;
import br.com.ericksantos.cupcakes.gateway.ResultadoGateway;
import br.com.ericksantos.cupcakes.model.Pagamento;
import br.com.ericksantos.cupcakes.model.Pedido;
import br.com.ericksantos.cupcakes.model.enums.Bandeira;
import br.com.ericksantos.cupcakes.model.enums.FormaPagamento;
import br.com.ericksantos.cupcakes.model.enums.StatusPagamento;
import br.com.ericksantos.cupcakes.model.enums.StatusPedido;
import br.com.ericksantos.cupcakes.repository.PagamentoRepository;
import br.com.ericksantos.cupcakes.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PagamentoService {

    /** US-10: validade do código PIX */
    static final int PIX_VALIDADE_MINUTOS = 30;

    private final PedidoRepository pedidoRepository;
    private final PagamentoRepository pagamentoRepository;
    private final ValidadorCartao validador;
    private final GatewayPagamento gateway;

    /**
     * US-09. Recusa do gateway NÃO é exceção: é um resultado válido, gravado como RECUSADO.
     */
    @Transactional
    public PagamentoResponse pagarComCartao(String email, Long pedidoId, PagamentoCartaoRequest req) {
        Pedido pedido = verificacaoBasica(pedidoId, email);

        if (req.forma() == FormaPagamento.PIX) {
            throw new RegraNegocioException("Para PIX, use a opção de pagamento por PIX.");
        }

        Bandeira bandeira = validador.validar(req.numero(), req.validade(), req.cvv());
        String digitos = req.numero().replaceAll("\\D", "");

        ResultadoGateway resultado = gateway.autorizar(digitos, req.cvv(), pedido.getTotal(), req.forma());

        Pagamento pagamento = pagamentoRepository.findByPedidoId(pedidoId).orElseGet(Pagamento::new);
        pagamento.setPedido(pedido);
        pagamento.setForma(req.forma());
        pagamento.setValor(pedido.getTotal());
        pagamento.setBandeira(bandeira.name());
        pagamento.setUltimosDigitos(digitos.substring(digitos.length() - 4));
        pagamento.setPixCopiaCola(null);   // a linha pode ter sido um PIX antes
        pagamento.setPixExpiraEm(null);
        pagamento.setStatus(resultado.aprovado() ? StatusPagamento.APROVADO : StatusPagamento.RECUSADO);

        if (resultado.aprovado()) {
            pedido.setStatus(StatusPedido.PAGO);
        }

        return PagamentoResponse.de(pagamentoRepository.save(pagamento), resultado.motivo());
    }

    /**
     * US-10: gera a cobrança PIX (status PENDENTE). O pedido continua AGUARDANDO_PAGAMENTO
     * até o banco confirmar (simulado em confirmarPix).
     * Se já existe um PIX pendente e válido, devolve o MESMO código: senão o cliente
     * poderia pagar um código antigo que a loja não reconhece mais.
     */
    @Transactional
    public PagamentoResponse pagarComPix(String email, Long pedidoId) {
        Pedido pedido = verificacaoBasica(pedidoId, email);
        Pagamento pagamento = pagamentoRepository.findByPedidoId(pedidoId).orElseGet(Pagamento::new);

        if (pixPendenteValido(pagamento)) {
            return PagamentoResponse.de(pagamento, null);
        }

        pagamento.setPedido(pedido);
        pagamento.setForma(FormaPagamento.PIX);
        pagamento.setStatus(StatusPagamento.PENDENTE);
        pagamento.setValor(pedido.getTotal());
        pagamento.setBandeira(null);         // a linha pode ter sido um cartão recusado antes
        pagamento.setUltimosDigitos(null);
        pagamento.setPixCopiaCola(gerarCopiaCola(pedido));
        pagamento.setPixExpiraEm(LocalDateTime.now().plusMinutes(PIX_VALIDADE_MINUTOS));

        return PagamentoResponse.de(pagamentoRepository.save(pagamento), null);
    }

    /**
     * US-10 (SIMULAÇÃO): faz o papel do banco avisando a loja que o PIX foi pago.
     * Na vida real seria um webhook chamado pelo PSP, não pelo cliente.
     *
     * noRollbackFor: quando o PIX expirou, o status EXPIRADO precisa ficar gravado
     * MESMO lançando a exceção (sem isso, o rollback desfaria a mudança).
     */
    @Transactional(noRollbackFor = RegraNegocioException.class)
    public PagamentoResponse confirmarPix(String email, Long pedidoId) {
        Pedido pedido = verificacaoBasica(pedidoId, email);

        Pagamento pagamento = pagamentoRepository.findByPedidoId(pedidoId)
            .filter(p -> p.getForma() == FormaPagamento.PIX && p.getStatus() == StatusPagamento.PENDENTE)
            .orElseThrow(() -> new RegraNegocioException("Não há PIX pendente para este pedido."));

        if (pagamento.getPixExpiraEm().isBefore(LocalDateTime.now())) {
            pagamento.setStatus(StatusPagamento.EXPIRADO);
            throw new RegraNegocioException("PIX expirado. Gere um novo código.");
        }

        pagamento.setStatus(StatusPagamento.APROVADO);
        pedido.setStatus(StatusPedido.PAGO);
        return PagamentoResponse.de(pagamento, null);
    }

    private Pedido verificacaoBasica(Long pedidoId, String email) {
        Pedido pedido = pedidoRepository.findByIdAndUsuarioEmail(pedidoId, email)
            .orElseThrow(() -> new NaoEncontradoException("Pedido não encontrado."));

        if (pedido.getStatus() != StatusPedido.AGUARDANDO_PAGAMENTO) {
            throw new RegraNegocioException("Este pedido não está aguardando pagamento.");
        }
        return pedido;
    }

    private boolean pixPendenteValido(Pagamento pagamento) {
        return pagamento.getForma() == FormaPagamento.PIX
            && pagamento.getStatus() == StatusPagamento.PENDENTE
            && pagamento.getPixExpiraEm().isAfter(LocalDateTime.now());
    }

    /**
     * SIMULADO: não é um BR Code real (EMV + CRC16 do Banco Central).
     * Contém o número do pedido, o valor e um identificador único da cobrança (txid).
     */
    private String gerarCopiaCola(Pedido pedido) {
        String txid = UUID.randomUUID().toString().replace("-", "");
        return "PIXSIMULADO.CUPCAKES|%s|%s|%s".formatted(pedido.getNumero(), pedido.getTotal(), txid);
    }
}
