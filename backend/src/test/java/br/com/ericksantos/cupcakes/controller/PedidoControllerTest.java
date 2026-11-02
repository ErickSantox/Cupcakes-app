package br.com.ericksantos.cupcakes.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

import br.com.ericksantos.cupcakes.model.Endereco;
import br.com.ericksantos.cupcakes.model.Produto;
import br.com.ericksantos.cupcakes.model.Usuario;
import br.com.ericksantos.cupcakes.repository.EnderecoRepository;
import br.com.ericksantos.cupcakes.repository.ProdutoRepository;
import br.com.ericksantos.cupcakes.repository.UsuarioRepository;
import jakarta.persistence.EntityManager;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class PedidoControllerTest {

    private static final String ANA = "ana@teste.com";
    private static final String BRUNO = "bruno@teste.com";

    @Autowired private MockMvc mockMvc;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private EnderecoRepository enderecoRepository;
    @Autowired private ProdutoRepository produtoRepository;
    @Autowired private EntityManager entityManager;

    private Usuario ana;
    private Usuario bruno;
    private Endereco enderecoAna;

    /** Do seed V2: estoque 40, R$ 9,90 */
    private Produto baunilha;
    /** Do seed V2: estoque 25, R$ 11,50 */
    private Produto doceDeLeite;

    @BeforeEach
    void setUp() {
        ana = criarUsuario("Ana", ANA);
        bruno = criarUsuario("Bruno", BRUNO);
        enderecoAna = criarEndereco(ana);
        baunilha = produtoPorNome("Baunilha Clássico");
        doceDeLeite = produtoPorNome("Doce de Leite");
    }

    // ---------- helpers ----------

    private Usuario criarUsuario(String nome, String email) {
        Usuario u = new Usuario();
        u.setNome(nome);
        u.setSobrenome("Teste");
        u.setTelefone("11999999999");
        u.setEmail(email);
        u.setSenhaHash("hash-qualquer");
        return usuarioRepository.save(u);
    }

    /** CEP começando com 0 = região SP (frete R$ 8,00) */
    private Endereco criarEndereco(Usuario dono) {
        Endereco e = new Endereco();
        e.setUsuario(dono);
        e.setCep("01310100");
        e.setLogradouro("Av. Paulista");
        e.setNumero("1000");
        e.setBairro("Bela Vista");
        e.setCidade("São Paulo");
        e.setUf("SP");
        e.setPadrao(true);
        return enderecoRepository.save(e);
    }

    private Produto produtoPorNome(String nome) {
        return produtoRepository.findAll().stream()
            .filter(p -> p.getNome().equals(nome))
            .findFirst()
            .orElseThrow();
    }

    /** Lê o produto direto do banco, ignorando o cache do Hibernate */
    private int estoqueAtual(Produto produto) {
        entityManager.flush();
        entityManager.clear();
        return produtoRepository.findById(produto.getId()).orElseThrow().getEstoque();
    }

    private static RequestPostProcessor logadoComo(String email) {
        return jwt().jwt(j -> j.subject(email));
    }

    private static String item(Long produtoId, int quantidade) {
        return """
            {"produtoId":%d,"quantidade":%d}""".formatted(produtoId, quantidade);
    }

    private static String pedido(Long enderecoId, String... itens) {
        return """
            {"enderecoId":%d,"itens":[%s]}""".formatted(enderecoId, String.join(",", itens));
    }

    private Long criarPedidoDaAna() throws Exception {
        String resposta = mockMvc.perform(post("/api/pedidos").with(logadoComo(ANA))
                .contentType(MediaType.APPLICATION_JSON)
                .content(pedido(enderecoAna.getId(), item(baunilha.getId(), 2))))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(resposta, "$.id")).longValue();
    }

    // ---------- segurança ----------

    @Test
    void semTokenDeveRetornar401() throws Exception {
        mockMvc.perform(post("/api/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(pedido(enderecoAna.getId(), item(baunilha.getId(), 1))))
            .andExpect(status().isUnauthorized());
    }

    // ---------- caminho feliz ----------

    @Test
    void criarPedidoDeveRetornar201ComValoresCalculados() throws Exception {
        // 2 × 9,90 = 19,80 de subtotal + 8,00 de frete (SP) = 27,80
        mockMvc.perform(post("/api/pedidos").with(logadoComo(ANA))
                .contentType(MediaType.APPLICATION_JSON)
                .content(pedido(enderecoAna.getId(), item(baunilha.getId(), 2))))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", matchesPattern("/api/pedidos/\\d+")))
            .andExpect(jsonPath("$.numero").value(matchesPattern("CPK-\\d{4}-\\d{6}")))
            .andExpect(jsonPath("$.status").value("AGUARDANDO_PAGAMENTO"))
            .andExpect(jsonPath("$.subtotal").value(19.80))
            .andExpect(jsonPath("$.frete").value(8.00))
            .andExpect(jsonPath("$.total").value(27.80))
            .andExpect(jsonPath("$.entregaCep").value("01310100"))
            .andExpect(jsonPath("$.itens", hasSize(1)))
            .andExpect(jsonPath("$.itens[0].precoUnitario").value(9.90))
            .andExpect(jsonPath("$.itens[0].subtotal").value(19.80));
    }

    @Test
    void criarPedidoDeveBaixarOEstoque() throws Exception {
        criarPedidoDaAna();

        assertThat(estoqueAtual(baunilha)).isEqualTo(38);
    }

    @Test
    void precoDoItemFicaCongeladoMesmoSeOProdutoMudar() throws Exception {
        Long id = criarPedidoDaAna();

        // Admin aumenta o preço depois do pedido
        Produto p = produtoRepository.findById(baunilha.getId()).orElseThrow();
        p.setPreco(new BigDecimal("99.90"));
        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(get("/api/pedidos/" + id).with(logadoComo(ANA)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.itens[0].precoUnitario").value(9.90))
            .andExpect(jsonPath("$.total").value(27.80));
    }

    // ---------- regras de negócio (422) ----------

    @Test
    void estoqueInsuficienteDeveRetornar422() throws Exception {
        mockMvc.perform(post("/api/pedidos").with(logadoComo(ANA))
                .contentType(MediaType.APPLICATION_JSON)
                .content(pedido(enderecoAna.getId(), item(doceDeLeite.getId(), 26))))
            .andExpect(status().isUnprocessableEntity());

        assertThat(estoqueAtual(doceDeLeite)).isEqualTo(25);
    }

    @Test
    void produtoInativoDeveRetornar422() throws Exception {
        Produto p = produtoRepository.findById(baunilha.getId()).orElseThrow();
        p.setAtivo(false);
        entityManager.flush();

        mockMvc.perform(post("/api/pedidos").with(logadoComo(ANA))
                .contentType(MediaType.APPLICATION_JSON)
                .content(pedido(enderecoAna.getId(), item(baunilha.getId(), 1))))
            .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void produtoRepetidoNoPedidoDeveRetornar422() throws Exception {
        mockMvc.perform(post("/api/pedidos").with(logadoComo(ANA))
                .contentType(MediaType.APPLICATION_JSON)
                .content(pedido(enderecoAna.getId(),
                    item(baunilha.getId(), 50), item(baunilha.getId(), 50))))
            .andExpect(status().isUnprocessableEntity());
    }

    // ---------- validação (400) ----------

    @Test
    void pedidoSemItensDeveRetornar400() throws Exception {
        mockMvc.perform(post("/api/pedidos").with(logadoComo(ANA))
                .contentType(MediaType.APPLICATION_JSON)
                .content(pedido(enderecoAna.getId())))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.campos.itens").exists());
    }

    @Test
    void quantidadeAcimaDe50DeveRetornar400() throws Exception {
        mockMvc.perform(post("/api/pedidos").with(logadoComo(ANA))
                .contentType(MediaType.APPLICATION_JSON)
                .content(pedido(enderecoAna.getId(), item(baunilha.getId(), 51))))
            .andExpect(status().isBadRequest());
    }

    // ---------- 🔒 IDOR ----------

    @Test
    void naoPodeUsarEnderecoDeOutroUsuario() throws Exception {
        mockMvc.perform(post("/api/pedidos").with(logadoComo(BRUNO))
                .contentType(MediaType.APPLICATION_JSON)
                .content(pedido(enderecoAna.getId(), item(baunilha.getId(), 1))))
            .andExpect(status().isNotFound());
    }

    @Test
    void naoPodeVerPedidoDeOutroUsuario() throws Exception {
        Long idDaAna = criarPedidoDaAna();

        mockMvc.perform(get("/api/pedidos/" + idDaAna).with(logadoComo(BRUNO)))
            .andExpect(status().isNotFound());
    }

    // ---------- atomicidade (rollback) ----------

    /**
     * Roda SEM a transação do teste: só assim o rollback do service acontece de verdade.
     * (Com @Transactional na classe, o service participaria da transação do teste
     * e a baixa do 1º item só seria desfeita no fim do teste.)
     * Por isso limpa manualmente o que o @BeforeEach gravou.
     */
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void falhaNoSegundoItemDesfazABaixaDoPrimeiro() throws Exception {
        try {
            int estoqueAntes = produtoRepository.findById(baunilha.getId()).orElseThrow().getEstoque();

            // 1º item tem estoque; 2º item não (pede 26, só tem 25)
            mockMvc.perform(post("/api/pedidos").with(logadoComo(ANA))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(pedido(enderecoAna.getId(),
                        item(baunilha.getId(), 2), item(doceDeLeite.getId(), 26))))
                .andExpect(status().isUnprocessableEntity());

            int estoqueDepois = produtoRepository.findById(baunilha.getId()).orElseThrow().getEstoque();
            assertThat(estoqueDepois).isEqualTo(estoqueAntes);
        } finally {
            enderecoRepository.deleteById(enderecoAna.getId());
            usuarioRepository.deleteById(ana.getId());
            usuarioRepository.deleteById(bruno.getId());
        }
    }
}
