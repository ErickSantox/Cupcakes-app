package br.com.ericksantos.cupcakes.repository;

import br.com.ericksantos.cupcakes.model.Produto;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

public interface ProdutoRepository extends JpaRepository<Produto,Long> {

    @EntityGraph(attributePaths = "categoria")
    @Query("""
        SELECT p FROM Produto p
        WHERE p.ativo = true
                AND (:categoriaId IS NULL OR p.categoria.id = :categoriaId)
                AND (:busca IS NULL
                     OR LOWER(p.nome)      LIKE LOWER(CONCAT('%', CAST(:busca AS String), '%'))
                     OR LOWER(p.descricao) LIKE LOWER(CONCAT('%', CAST(:busca AS String), '%')))
                    ORDER BY p.nome
        """)
    List<Produto> buscarVitrine(@Param("categoriaId") Long categoriaId, @Param("busca") String busca);

    @EntityGraph(attributePaths = "categoria")
    Optional<Produto> findByIdAndAtivoTrue(Long id);

    @EntityGraph(attributePaths = "categoria")
    List<Produto> findAllByOrderByNomeAsc();
}
