package br.com.ericksantos.cupcakes.dto;

import br.com.ericksantos.cupcakes.model.Categoria;
import br.com.ericksantos.cupcakes.model.Produto;

import java.math.BigDecimal;

public record ProdutoResponse(Long id, String nome, String descricao, BigDecimal preco, String fotoUrl, int estoque, Long categoriaId, String categoriaNome, boolean disponivel) {

    public static ProdutoResponse de(Produto p) {
        return new ProdutoResponse(p.getId(), p.getNome(), p.getDescricao(), p.getPreco(),p.getFotoUrl(),p.getEstoque(),p.getCategoria().getId(),p.getCategoria().getNome(),p.getEstoque() > 0);
    }
}
