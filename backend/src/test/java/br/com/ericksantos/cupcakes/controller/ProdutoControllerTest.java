package br.com.ericksantos.cupcakes.controller;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import br.com.ericksantos.cupcakes.model.Categoria;
import br.com.ericksantos.cupcakes.model.Produto;
import br.com.ericksantos.cupcakes.repository.CategoriaRepository;
import br.com.ericksantos.cupcakes.repository.ProdutoRepository;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class ProdutoControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ProdutoRepository produtoRepository;
    @Autowired private CategoriaRepository categoriaRepository;

    // Vitrine é pública: visitante sem login vê os produtos
    @Test
    void deveListarProdutosSemToken() throws Exception {
        mockMvc.perform(get("/api/produtos"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isNotEmpty());
    }

    // US-04 RN#1: produto inativo não aparece na vitrine
    @Test
    void naoDeveListarProdutoInativo() throws Exception {
        Produto produto = desativarPrimeiroProduto();

        mockMvc.perform(get("/api/produtos"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.nome == '" + produto.getNome() + "')]").isEmpty());
    }

    // US-04 CA#2: produto sem estoque aparece como indisponível
    @Test
    void deveMarcarProdutoSemEstoqueComoIndisponivel() throws Exception {
        mockMvc.perform(get("/api/produtos"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.nome == 'Red Velvet com Morango')].disponivel").value(contains(false)));
    }

    // US-04 CA#3: filtro por categoria
    @Test
    void deveFiltrarPorCategoria() throws Exception {
        Categoria chocolate = categoriaRepository.findAll().stream()
            .filter(c -> c.getNome().equals("Chocolate"))
            .findFirst().orElseThrow();

        mockMvc.perform(get("/api/produtos").param("categoriaId", chocolate.getId().toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isNotEmpty())
            .andExpect(jsonPath("$[*].categoriaId", everyItem(is(chocolate.getId().intValue()))));
    }

    // Detalhe de um produto ativo
    @Test
    void deveBuscarProdutoPorId() throws Exception {
        Produto produto = produtoRepository.findByAtivoTrueOrderByNomeAsc().get(0);

        mockMvc.perform(get("/api/produtos/" + produto.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nome").value(produto.getNome()));
    }

    // Produto inativo "não existe" para o cliente → 404
    @Test
    void deveRetornar404AoBuscarProdutoInativo() throws Exception {
        Produto produto = desativarPrimeiroProduto();

        mockMvc.perform(get("/api/produtos/" + produto.getId()))
            .andExpect(status().isNotFound());
    }

    // Categorias também são públicas (alimentam o filtro da vitrine)
    @Test
    void deveListarCategoriasSemToken() throws Exception {
        mockMvc.perform(get("/api/categorias"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(4)));
    }

    private Produto desativarPrimeiroProduto() {
        Produto produto = produtoRepository.findByAtivoTrueOrderByNomeAsc().get(0);
        produto.setAtivo(false);
        return produtoRepository.save(produto);
    }
}
