package br.com.ericksantos.cupcakes.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import br.com.ericksantos.cupcakes.repository.CategoriaRepository;
import br.com.ericksantos.cupcakes.repository.ProdutoRepository;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class AdminProdutoControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private CategoriaRepository categoriaRepository;
    @Autowired private ProdutoRepository produtoRepository;

    private Long categoriaId;
    private Long produtoId;

    @BeforeEach
    void setUp() {
        // Usa os IDs reais do seed (V2), em vez de supor que são 1
        categoriaId = categoriaRepository.findAll().get(0).getId();
        produtoId = produtoRepository.findAll().get(0).getId();
    }

    /** Simula um usuário logado com perfil ADMIN, sem precisar fazer login */
    private static RequestPostProcessor admin() {
        return jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    private static RequestPostProcessor cliente() {
        return jwt().authorities(new SimpleGrantedAuthority("ROLE_CLIENTE"));
    }

    private String json(String nome, String preco, Long catId, String fotoUrl) {
        return """
            {"nome":"%s","descricao":"Descrição de teste","preco":%s,
             "categoriaId":%d,"fotoUrl":"%s","estoque":10}
            """.formatted(nome, preco, catId, fotoUrl);
    }

    private String valido() {
        return json("Cupcake Teste", "12.50", categoriaId, "https://exemplo.com/foto.jpg");
    }

    // ---------- Segurança ----------

    @Test
    void semTokenDeveRetornar401() throws Exception {
        mockMvc.perform(post("/api/admin/produtos")
                .contentType(MediaType.APPLICATION_JSON).content(valido()))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void clienteNaoPodeCadastrar403() throws Exception {
        mockMvc.perform(post("/api/admin/produtos").with(cliente())
                .contentType(MediaType.APPLICATION_JSON).content(valido()))
            .andExpect(status().isForbidden());
    }

    // ---------- Cadastro (CA#1 e CA#2) ----------

    @Test
    void adminCadastraEProdutoApareceNaVitrine() throws Exception {
        mockMvc.perform(post("/api/admin/produtos").with(admin())
                .contentType(MediaType.APPLICATION_JSON).content(valido()))
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location"))
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.nome").value("Cupcake Teste"));

        // CA#2: aparece na vitrine na hora, sem login
        mockMvc.perform(get("/api/produtos").param("busca", "Cupcake Teste"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)));
    }

    // ---------- Validações (RN#1 e RN#2) ----------

    @Test
    void precoZeroDeveRetornar400() throws Exception {
        mockMvc.perform(post("/api/admin/produtos").with(admin())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json("Cupcake Teste", "0", categoriaId, "https://exemplo.com/foto.jpg")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.campos.preco").exists());
    }

    @Test
    void semFotoDeveRetornar400() throws Exception {
        mockMvc.perform(post("/api/admin/produtos").with(admin())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json("Cupcake Teste", "12.50", categoriaId, "")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.campos.fotoUrl").exists());
    }

    @Test
    void categoriaInexistenteDeveRetornar404() throws Exception {
        mockMvc.perform(post("/api/admin/produtos").with(admin())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json("Cupcake Teste", "12.50", 999999L, "https://exemplo.com/foto.jpg")))
            .andExpect(status().isNotFound());
    }

    // ---------- Edição e desativação ----------

    @Test
    void adminAtualizaProduto() throws Exception {
        mockMvc.perform(put("/api/admin/produtos/" + produtoId).with(admin())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json("Nome Atualizado", "15.00", categoriaId, "https://exemplo.com/nova.jpg")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nome").value("Nome Atualizado"))
            .andExpect(jsonPath("$.preco").value(15.00));
    }

    @Test
    void atualizarProdutoInexistenteDeveRetornar404() throws Exception {
        mockMvc.perform(put("/api/admin/produtos/999999").with(admin())
                .contentType(MediaType.APPLICATION_JSON).content(valido()))
            .andExpect(status().isNotFound());
    }

    @Test
    void desativarRemoveDaVitrine() throws Exception {
        mockMvc.perform(patch("/api/admin/produtos/" + produtoId + "/ativo").with(admin())
                .contentType(MediaType.APPLICATION_JSON).content("{\"ativo\":false}"))
            .andExpect(status().isOk());

        // Some da vitrine pública...
        mockMvc.perform(get("/api/produtos/" + produtoId))
            .andExpect(status().isNotFound());

        // ...mas o admin continua vendo (soft delete)
        mockMvc.perform(get("/api/admin/produtos").with(admin()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.id == " + produtoId + ")]").exists());
    }
}
