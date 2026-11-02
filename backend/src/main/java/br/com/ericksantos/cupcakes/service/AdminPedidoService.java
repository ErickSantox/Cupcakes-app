package br.com.ericksantos.cupcakes.service;

import br.com.ericksantos.cupcakes.dto.PedidoResponse;
import br.com.ericksantos.cupcakes.exception.NaoEncontradoException;
import br.com.ericksantos.cupcakes.exception.RegraNegocioException;
import br.com.ericksantos.cupcakes.model.ItemPedido;
import br.com.ericksantos.cupcakes.model.Pedido;
import br.com.ericksantos.cupcakes.model.enums.StatusPagamento;
import br.com.ericksantos.cupcakes.model.enums.StatusPedido;
import br.com.ericksantos.cupcakes.repository.PagamentoRepository;
import br.com.ericksantos.cupcakes.repository.PedidoRepository;
import br.com.ericksantos.cupcakes.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Pageable;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AdminPedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;
    private final PagamentoRepository pagamentoRepository;

    @Transactional
    public PedidoResponse atualizarStatus(Long id, StatusPedido destino) {
        Pedido pedido = pedidoRepository.findById(id)
            .orElseThrow(() -> new NaoEncontradoException("Pedido não encontrado."));

        if(destino == StatusPedido.PAGO){
            throw new RegraNegocioException("O status PAGO é definido pelo pagamento.");
        }

        if(!pedido.getStatus().podeIrPara(destino)){
            throw new RegraNegocioException("Não é possível mudar de " + pedido.getStatus() + " para " + destino + ".");
        }

        if(destino == StatusPedido.CANCELADO){
            cancelar(pedido);
        }

        pedido.setStatus(destino);

        return PedidoResponse.de(pedido);
    }

    private static final LocalDate DATA_MINIMA = LocalDate.of(2000, 1, 1);

    @Transactional(readOnly = true)
    public Page<PedidoResponse> listar(StatusPedido status, LocalDate de, LocalDate ate, Pageable pageable) {
        LocalDate inicio = de != null ? de : DATA_MINIMA;
        LocalDate fim = ate != null ? ate : LocalDate.now();

        if (inicio.isAfter(fim)) {
            throw new RegraNegocioException("A data inicial não pode ser posterior à data final.");
        }

        return pedidoRepository
            .filtrar(status, inicio.atStartOfDay(), fim.plusDays(1).atStartOfDay(), pageable)
            .map(PedidoResponse::de);
    }

    @Transactional(readOnly = true)
    public PedidoResponse buscar(Long id) {
        return pedidoRepository.findById(id)
            .map(PedidoResponse::de)
            .orElseThrow(() -> new NaoEncontradoException("Pedido não encontrado."));
    }

    private void cancelar(Pedido pedido) {
        for (ItemPedido item : pedido.getItens()) {
            int linhas = produtoRepository.devolverEstoque(item.getProduto().getId(), item.getQuantidade());
            if (linhas == 0) {
                throw new IllegalStateException("Produto " + item.getProduto().getId() + " não encontrado ao devolver estoque.");
            }
        }

        pagamentoRepository.findByPedidoId(pedido.getId()).ifPresent(pagamento -> {
            if (pagamento.getStatus() == StatusPagamento.APROVADO) {
                pagamento.setStatus(StatusPagamento.ESTORNADO);
            } else if (pagamento.getStatus() == StatusPagamento.PENDENTE) {
                pagamento.setStatus(StatusPagamento.EXPIRADO);
            }
        });
    }
}
