package br.com.ericksantos.cupcakes.repository;

import br.com.ericksantos.cupcakes.model.Categoria;
import br.com.ericksantos.cupcakes.model.Produto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;


@DataJpaTest
@ActiveProfiles("test")
public class ProdutoRepositoryTest {

    @Autowired ProdutoRepository produtoRepository;
    @Autowired CategoriaRepository categoriaRepository;

    @Test
    void deveCarregarProdutosDoSeed(){
        assertThat(produtoRepository.findAll()).isNotEmpty();
    }

    @Test
    void naoDeveSalvarProdutoComPrecoZero(){
        Produto p = new Produto();

        p.setCategoria((Categoria) categoriaRepository.findAll().get(0));
        p.setNome("Teste");
        p.setDescricao("Teste");
        p.setEstoque(1);
        p.setPreco(BigDecimal.ZERO);
        p.setFotoUrl("/x.jpg");

        assertThatThrownBy(() -> produtoRepository.saveAndFlush(p))
            .isInstanceOf(DataIntegrityViolationException.class);

    }

}
