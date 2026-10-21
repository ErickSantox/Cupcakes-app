package br.com.ericksantos.cupcakes.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.ericksantos.cupcakes.exception.NaoEncontradoException;
import br.com.ericksantos.cupcakes.repository.CategoriaRepository;
import br.com.ericksantos.cupcakes.repository.ProdutoRepository;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

    @Mock private ProdutoRepository produtoRepository;
    @Mock private CategoriaRepository categoriaRepository;
    @InjectMocks private ProdutoService produtoService;

    @Test
    void deveRepassarFiltrosParaAConsulta() {
        when(produtoRepository.buscarVitrine(2L, "chocolate")).thenReturn(List.of());

        produtoService.listar(2L, "  chocolate  ");

        verify(produtoRepository).buscarVitrine(2L, "chocolate");   // trim aplicado
    }

    @Test
    void deveTratarBuscaEmBrancoComoNula() {
        when(produtoRepository.buscarVitrine(null, null)).thenReturn(List.of());

        produtoService.listar(null, "   ");

        verify(produtoRepository).buscarVitrine(null, null);         // filtro desligado
    }

    @Test
    void deveLancarNaoEncontradoQuandoProdutoNaoExisteOuInativo() {
        when(produtoRepository.findByIdAndAtivoTrue(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> produtoService.buscar(99L))
            .isInstanceOf(NaoEncontradoException.class)
            .hasMessage("Produto não encontrado.");
    }
}
