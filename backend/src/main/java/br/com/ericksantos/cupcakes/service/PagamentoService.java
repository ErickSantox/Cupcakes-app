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

@Service
@RequiredArgsConstructor
public class PagamentoService {

    private final PedidoRepository pedidoRepository;
    private final PagamentoRepository pagamentoRepository;
    private final ValidadorCartao validador;
    private final GatewayPagamento gateway;


    @Transactional
    public PagamentoResponse pagarComCartao(String email, Long pedidoId, PagamentoCartaoRequest req){

        Pedido pedido = pedidoRepository.findByIdAndUsuarioEmail(pedidoId,email)
            .orElseThrow(() -> new NaoEncontradoException("Pedido não encontrado."));

        if (req.forma().equals(FormaPagamento.PIX)){
            throw new RegraNegocioException("Para PIX, use a opção de pagamento por PIX.");
        }

        if (pedido.getStatus() != StatusPedido.AGUARDANDO_PAGAMENTO){
            throw new RegraNegocioException("Este pedido não está aguardando pagamento.");
        }

        Bandeira bandeira = validador.validar(req.numero(),req.validade(),req.cvv());

        String digitos = req.numero().replaceAll("\\D","");

        ResultadoGateway resultado = gateway.autorizar(digitos, req.cvv(), pedido.getTotal(),req.forma());

        Pagamento pagamento = pagamentoRepository.findByPedidoId(pedidoId).orElseGet(Pagamento::new);

        pagamento.setPedido(pedido);
        pagamento.setForma(req.forma());
        pagamento.setValor(pedido.getTotal());
        pagamento.setBandeira(bandeira.name());
        pagamento.setUltimosDigitos(digitos.substring(digitos.length() - 4));
        pagamento.setStatus(resultado.aprovado() ? StatusPagamento.APROVADO : StatusPagamento.RECUSADO);

        if (resultado.aprovado()){
            pedido.setStatus(StatusPedido.PAGO);
        }

        return PagamentoResponse.de(pagamentoRepository.save(pagamento), resultado.motivo());
    }

}
