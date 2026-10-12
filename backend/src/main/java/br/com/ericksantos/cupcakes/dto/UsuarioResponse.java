package br.com.ericksantos.cupcakes.dto;

import br.com.ericksantos.cupcakes.model.Usuario;
import br.com.ericksantos.cupcakes.model.enums.Perfil;

public record UsuarioResponse(Long id,String nome, String sobrenome, String telefone, String email, Perfil perfil) {

    public static UsuarioResponse de(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getNome(), u.getSobrenome(), u.getTelefone(), u.getEmail(), u.getPerfil());
    }
}
