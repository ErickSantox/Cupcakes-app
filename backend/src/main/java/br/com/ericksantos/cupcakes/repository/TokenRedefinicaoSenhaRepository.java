package br.com.ericksantos.cupcakes.repository;

import br.com.ericksantos.cupcakes.model.TokenRedefinicaoSenha;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenRedefinicaoSenhaRepository extends JpaRepository<TokenRedefinicaoSenha,Long> {
}
