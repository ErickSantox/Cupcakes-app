package br.com.ericksantos.cupcakes.dto;

import br.com.ericksantos.cupcakes.model.Categoria;

public record CategoriaResponse(Long id, String nome) {

    public static CategoriaResponse de(Categoria c) {
        return new CategoriaResponse(c.getId(), c.getNome());
    }
}
