package br.com.ericksantos.cupcakes.repository;

import br.com.ericksantos.cupcakes.model.Produto;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

public interface ProdutoRepository extends JpaRepository<Produto,Long> {

    @EntityGraph(attributePaths = "categoria")
    List<Produto> findByAtivoTrueOrderByNomeAsc();

    @EntityGraph(attributePaths = "categoria")
    List<Produto> findByAtivoTrueAndCategoriaIdOrderByNomeAsc(Long categoriaId);

    @EntityGraph(attributePaths = "categoria")
    Optional<Produto> findByIdAndAtivoTrue(Long id);
}
