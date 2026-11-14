package br.com.ericksantos.cupcakes.repository;

import br.com.ericksantos.cupcakes.model.TokenRedefinicaoSenha;
import br.com.ericksantos.cupcakes.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TokenRedefinicaoSenhaRepository extends JpaRepository<TokenRedefinicaoSenha,Long> {

    Optional<TokenRedefinicaoSenha> findByTokenHash(String tokenHash);

    List<TokenRedefinicaoSenha> findByUsuarioAndUsadoEmIsNull(Usuario usuario);
}
