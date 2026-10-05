package br.com.ericksantos.cupcakes.repository;

import br.com.ericksantos.cupcakes.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdutoRepository extends JpaRepository<Produto,Long> {
}
