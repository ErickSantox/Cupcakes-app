package br.com.ericksantos.cupcakes.service;


import br.com.ericksantos.cupcakes.dto.ItemPedidoRequest;
import br.com.ericksantos.cupcakes.dto.PedidoRequest;
import br.com.ericksantos.cupcakes.dto.PedidoResponse;
import br.com.ericksantos.cupcakes.exception.NaoEncontradoException;
import br.com.ericksantos.cupcakes.exception.RegraNegocioException;
import br.com.ericksantos.cupcakes.model.*;
import br.com.ericksantos.cupcakes.model.enums.StatusPedido;
import br.com.ericksantos.cupcakes.repository.EnderecoRepository;
import br.com.ericksantos.cupcakes.repository.PedidoRepository;
import br.com.ericksantos.cupcakes.repository.ProdutoRepository;
import br.com.ericksantos.cupcakes.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Year;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProdutoRepository produtoRepository;
    private final EnderecoRepository enderecoRepository;
    private final FreteService freteService;

    @Transactional
    public PedidoResponse criar(String email, PedidoRequest req) {
        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new NaoEncontradoException("E-mail nao encontrado") );

        Endereco endereco = enderecoRepository.findByIdAndUsuarioEmail(req.enderecoId(), email)
            .orElseThrow(()-> new NaoEncontradoException("Endereço não encontrado."));

        long idsDistintos = req.itens().stream()
            .map(ItemPedidoRequest::produtoId)
            .distinct()
            .count();

        if (idsDistintos != req.itens().size()) {
            throw new RegraNegocioException("Cada produto deve aparecer só uma vez no pedido");
        }

        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);

        pedido.setStatus(StatusPedido.AGUARDANDO_PAGAMENTO);

        BigDecimal subtotal = BigDecimal.ZERO;

        for(ItemPedidoRequest itemReq : req.itens()) {
            Produto produto = produtoRepository.findByIdAndAtivoTrue(itemReq.produtoId())
                .orElseThrow(() -> new RegraNegocioException("Produto indisponível."));

            int linhasAfetadas = produtoRepository.baixarEstoque(produto.getId(), itemReq.quantidade());
            if (linhasAfetadas == 0) {
                throw new RegraNegocioException("Estoque insuficiente para " + produto.getNome() + ".");
            }

            ItemPedido item = new ItemPedido();
            item.setProduto(produto);
            item.setQuantidade(itemReq.quantidade());
            item.setPrecoUnitario(produto.getPreco());
            pedido.adicionarItem(item);

            subtotal = subtotal.add(produto.getPreco().multiply(BigDecimal.valueOf(itemReq.quantidade())));
        }

        BigDecimal frete = freteService.calcular(endereco.getCep(), subtotal).valor();
        pedido.setSubtotal(subtotal);
        pedido.setFrete(frete);
        pedido.setTotal(subtotal.add(frete));

        pedido.setEntregaLogradouro(endereco.getLogradouro());
        pedido.setEntregaNumero(endereco.getNumero());
        pedido.setEntregaComplemento(endereco.getComplemento());
        pedido.setEntregaBairro(endereco.getBairro());
        pedido.setEntregaCidade(endereco.getCidade());
        pedido.setEntregaUf(endereco.getUf());
        pedido.setEntregaCep(endereco.getCep());

        pedido.setNumero("CPK-%d-%06d".formatted(Year.now().getValue(), pedidoRepository.proximoNumero()));
        return PedidoResponse.de(pedidoRepository.save(pedido));
    }

    @Transactional(readOnly = true)
    public PedidoResponse buscar(String email, Long id) {
        return pedidoRepository.findByIdAndUsuarioEmail(id,email).map(PedidoResponse::de)
            .orElseThrow(() -> new NaoEncontradoException("Pedido não encontrado."));
    }
}
