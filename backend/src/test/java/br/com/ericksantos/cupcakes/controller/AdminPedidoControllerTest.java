package br.com.ericksantos.cupcakes.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

import br.com.ericksantos.cupcakes.model.Endereco;
import br.com.ericksantos.cupcakes.model.Produto;
import br.com.ericksantos.cupcakes.model.Usuario;
import br.com.ericksantos.cupcakes.model.enums.StatusPagamento;
import br.com.ericksantos.cupcakes.repository.EnderecoRepository;
import br.com.ericksantos.cupcakes.repository.PagamentoRepository;
import br.com.ericksantos.cupcakes.repository.ProdutoRepository;
import br.com.ericksantos.cupcakes.repository.UsuarioRepository;
import jakarta.persistence.EntityManager;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class AdminPedidoControllerTest {

    private static final String ANA = "ana@teste.com";
    private static final String URL = "/api/admin/pedidos";
    private static final int QTD_NO_PEDIDO = 2;

    @Autowired private MockMvc mockMvc;
    @Autowired private EntityManager entityManager;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private EnderecoRepository enderecoRepository;
    @Autowired private ProdutoRepository produtoRepository;
    @Autowired private PagamentoRepository pagamentoRepository;

    private Endereco endereco;
    private Produto baunilha;
    private int estoqueInicial;
    private Long pedidoId;

    @BeforeEach
    void setUp() throws Exception {
        Usuario ana = criarUsuario();
        endereco = criarEndereco(ana);
        baunilha = produtoRepository.findAll().stream()
            .filter(p -> p.getNome().equals("Baunilha Clássico"))
            .findFirst().orElseThrow();
        estoqueInicial = baunilha.getEstoque();
        pedidoId = criarPedido(); // nasce AGUARDANDO_PAGAMENTO
    }

    // ---------- helpers ----------

    private Usuario criarUsuario() {
        Usuario u = new Usuario();
        u.setNome("Ana");
        u.setSobrenome("Teste");
        u.setTelefone("11999999999");
        u.setEmail(ANA);
        u.setSenhaHash("hash-qualquer");
        return usuarioRepository.save(u);
    }

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

    private Long criarPedido() throws Exception {
        String corpo = """
            {"enderecoId":%d,"itens":[{"produtoId":%d,"quantidade":%d}]}
            """.formatted(endereco.getId(), baunilha.getId(), QTD_NO_PEDIDO);

        String resposta = mockMvc.perform(post("/api/pedidos").with(cliente())
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(resposta, "$.id")).longValue();
    }

    /** Leva o pedido até PAGO pelo caminho real: gera o PIX e simula o banco confirmando. */
    private void pagarComPix() throws Exception {
        String url = "/api/pedidos/" + pedidoId + "/pagamento/pix";
        mockMvc.perform(post(url).with(cliente())).andExpect(status().isOk());
        mockMvc.perform(post(url + "/confirmar").with(cliente())).andExpect(status().isOk());
    }

    private ResultActions mudarStatus(String novoStatus, RequestPostProcessor quem) throws Exception {
        return mockMvc.perform(patch(URL + "/" + pedidoId + "/status").with(quem)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"status\":\"" + novoStatus + "\"}"));
    }

    /** O UPDATE de estoque é bulk e passa por fora do cache do Hibernate: limpa antes de ler. */
    private int estoqueAtual() {
        entityManager.flush();
        entityManager.clear();
        return produtoRepository.findById(baunilha.getId()).orElseThrow().getEstoque();
    }

    private StatusPagamento statusDoPagamento() {
        entityManager.flush();
        entityManager.clear();
        return pagamentoRepository.findByPedidoId(pedidoId).orElseThrow().getStatus();
    }

    private static RequestPostProcessor admin() {
        return jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    private static RequestPostProcessor cliente() {
        return jwt().jwt(j -> j.subject(ANA)).authorities(new SimpleGrantedAuthority("ROLE_CLIENTE"));
    }

    // ---------- PATCH: transições válidas ----------

    @Test
    void fluxoCompletoAteEntregue() throws Exception {
        pagarComPix();

        mudarStatus("EM_PREPARO", admin()).andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("EM_PREPARO"));
        mudarStatus("SAIU_PARA_ENTREGA", admin()).andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SAIU_PARA_ENTREGA"));
        mudarStatus("ENTREGUE", admin()).andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("ENTREGUE"));
    }

    @Test
    void cancelarPedidoPagoDeveEstornarEDevolverEstoque() throws Exception {
        pagarComPix();
        assertThat(estoqueAtual()).isEqualTo(estoqueInicial - QTD_NO_PEDIDO);

        mudarStatus("CANCELADO", admin())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("CANCELADO"));

        assertThat(estoqueAtual()).isEqualTo(estoqueInicial);
        assertThat(statusDoPagamento()).isEqualTo(StatusPagamento.ESTORNADO);
    }

    @Test
    void cancelarPedidoSemPagamentoDeveSoDevolverEstoque() throws Exception {
        mudarStatus("CANCELADO", admin()).andExpect(status().isOk());

        assertThat(estoqueAtual()).isEqualTo(estoqueInicial);
        assertThat(pagamentoRepository.findByPedidoId(pedidoId)).isEmpty();
    }

    @Test
    void cancelarComPixPendenteDeveExpirarOPix() throws Exception {
        mockMvc.perform(post("/api/pedidos/" + pedidoId + "/pagamento/pix").with(cliente()))
            .andExpect(status().isOk());

        mudarStatus("CANCELADO", admin()).andExpect(status().isOk());

        assertThat(statusDoPagamento()).isEqualTo(StatusPagamento.EXPIRADO);
    }

    // ---------- PATCH: transições inválidas (422) ----------

    @Test
    void adminNaoPodeMarcarComoPago() throws Exception {
        mudarStatus("PAGO", admin()).andExpect(status().isUnprocessableEntity());
    }

    @Test
    void naoPodePularEtapa() throws Exception {
        pagarComPix();
        mudarStatus("ENTREGUE", admin()).andExpect(status().isUnprocessableEntity());
    }

    @Test
    void naoPodePrepararPedidoNaoPago() throws Exception {
        mudarStatus("EM_PREPARO", admin()).andExpect(status().isUnprocessableEntity());
    }

    @Test
    void naoPodeCancelarPedidoQueSaiuParaEntrega() throws Exception {
        pagarComPix();
        mudarStatus("EM_PREPARO", admin()).andExpect(status().isOk());
        mudarStatus("SAIU_PARA_ENTREGA", admin()).andExpect(status().isOk());

        mudarStatus("CANCELADO", admin()).andExpect(status().isUnprocessableEntity());
    }

    @Test
    void pedidoCanceladoEhFinal() throws Exception {
        mudarStatus("CANCELADO", admin()).andExpect(status().isOk());
        mudarStatus("EM_PREPARO", admin()).andExpect(status().isUnprocessableEntity());
    }

    @Test
    void transicaoInvalidaNaoDeveAlterarEstoque() throws Exception {
        mudarStatus("CANCELADO", admin()).andExpect(status().isOk());
        mudarStatus("CANCELADO", admin()).andExpect(status().isUnprocessableEntity());

        // se o 2º cancelamento tivesse devolvido estoque, sobraria estoque a mais
        assertThat(estoqueAtual()).isEqualTo(estoqueInicial);
    }

    // ---------- PATCH: entrada inválida ----------

    @Test
    void statusInexistenteDeveRetornar400() throws Exception {
        mudarStatus("BANANA", admin()).andExpect(status().isBadRequest());
    }

    @Test
    void statusNuloDeveRetornar400() throws Exception {
        mockMvc.perform(patch(URL + "/" + pedidoId + "/status").with(admin())
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void pedidoInexistenteDeveRetornar404() throws Exception {
        mockMvc.perform(patch(URL + "/999999/status").with(admin())
                .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"CANCELADO\"}"))
            .andExpect(status().isNotFound());
    }

    // ---------- GET: listagem ----------

    @Test
    void listarSemFiltroDeveTrazerOPedido() throws Exception {
        mockMvc.perform(get(URL).with(admin()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[*].id", hasItem(pedidoId.intValue())))
            .andExpect(jsonPath("$.page.totalElements").exists());
    }

    @Test
    void filtrarPorStatus() throws Exception {
        mockMvc.perform(get(URL).param("status", "AGUARDANDO_PAGAMENTO").with(admin()))
            .andExpect(jsonPath("$.content[*].id", hasItem(pedidoId.intValue())));

        mockMvc.perform(get(URL).param("status", "ENTREGUE").with(admin()))
            .andExpect(jsonPath("$.content[*].id", not(hasItem(pedidoId.intValue()))));
    }

    @Test
    void filtrarPorHojeDeveIncluirPedidoFeitoAgora() throws Exception {
        String hoje = LocalDate.now().toString();
        mockMvc.perform(get(URL).param("de", hoje).param("ate", hoje).with(admin()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[*].id", hasItem(pedidoId.intValue())));
    }

    @Test
    void filtrarAPartirDeAmanhaNaoDeveTrazerNada() throws Exception {
        String amanha = LocalDate.now().plusDays(1).toString();
        mockMvc.perform(get(URL).param("de", amanha).param("ate", amanha).with(admin()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.page.totalElements").value(0));
    }

    @Test
    void dataInicialDepoisDaFinalDeveRetornar422() throws Exception {
        mockMvc.perform(get(URL).param("de", "2026-11-10").param("ate", "2026-11-01").with(admin()))
            .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void paginacaoDeveRespeitarSize() throws Exception {
        criarPedido();
        criarPedido();

        mockMvc.perform(get(URL).param("size", "2").with(admin()))
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.page.size").value(2));
    }

    // ---------- segurança (RN#1 / RNF#1) ----------

    @Test
    void clienteNaoPodeMudarStatus() throws Exception {
        mudarStatus("CANCELADO", cliente()).andExpect(status().isForbidden());
    }

    @Test
    void clienteNaoPodeListarPedidosAdmin() throws Exception {
        mockMvc.perform(get(URL).with(cliente())).andExpect(status().isForbidden());
    }

    @Test
    void semTokenDeveRetornar401() throws Exception {
        mockMvc.perform(get(URL)).andExpect(status().isUnauthorized());
    }

    @Test
    void adminBuscaPedidoDeQualquerCliente() throws Exception {
        mockMvc.perform(get(URL + "/" + pedidoId).with(admin()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(pedidoId.intValue()));
    }
}
