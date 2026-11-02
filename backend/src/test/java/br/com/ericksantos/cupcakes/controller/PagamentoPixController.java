package br.com.ericksantos.cupcakes.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

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
import br.com.ericksantos.cupcakes.model.Pagamento;
import br.com.ericksantos.cupcakes.model.Produto;
import br.com.ericksantos.cupcakes.model.Usuario;
import br.com.ericksantos.cupcakes.model.enums.StatusPagamento;
import br.com.ericksantos.cupcakes.repository.EnderecoRepository;
import br.com.ericksantos.cupcakes.repository.PagamentoRepository;
import br.com.ericksantos.cupcakes.repository.PedidoRepository;
import br.com.ericksantos.cupcakes.repository.ProdutoRepository;
import br.com.ericksantos.cupcakes.repository.UsuarioRepository;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class PagamentoPixControllerTest {

    private static final String ANA = "ana@teste.com";
    private static final String BRUNO = "bruno@teste.com";
    private static final int QTD_NO_PEDIDO = 2;

    @Autowired private MockMvc mockMvc;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private EnderecoRepository enderecoRepository;
    @Autowired private ProdutoRepository produtoRepository;
    @Autowired private PedidoRepository pedidoRepository;
    @Autowired private PagamentoRepository pagamentoRepository;

    private Usuario ana;
    private Usuario bruno;
    private Endereco endereco;
    private Produto baunilha;
    private Long pedidoDaAna;

    @BeforeEach
    void setUp() throws Exception {
        ana = criarUsuario("Ana", ANA);
        bruno = criarUsuario("Bruno", BRUNO);
        endereco = criarEndereco(ana);
        baunilha = produtoRepository.findAll().stream()
            .filter(p -> p.getNome().equals("Baunilha Clássico"))
            .findFirst().orElseThrow();
        pedidoDaAna = criarPedido();
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

        String resposta = mockMvc.perform(post("/api/pedidos").with(logadoComo(ANA))
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(resposta, "$.id")).longValue();
    }

    private static RequestPostProcessor logadoComo(String email) {
        return jwt().jwt(j -> j.subject(email));
    }

    private String urlPix() {
        return "/api/pedidos/" + pedidoDaAna + "/pagamento/pix";
    }

    private String gerarPix() throws Exception {
        return mockMvc.perform(post(urlPix()).with(logadoComo(ANA)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
    }

    /** "Volta no tempo": faz o PIX ter expirado há 1 minuto */
    private void expirarPix() {
        Pagamento p = pagamentoRepository.findByPedidoId(pedidoDaAna).orElseThrow();
        p.setPixExpiraEm(LocalDateTime.now().minusMinutes(1));
        pagamentoRepository.save(p);
    }

    // ---------- gerar ----------

    @Test
    void gerarPixDeveFicarPendenteComCodigoEExpiracao() throws Exception {
        mockMvc.perform(post(urlPix()).with(logadoComo(ANA)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.forma").value("PIX"))
            .andExpect(jsonPath("$.status").value("PENDENTE"))
            .andExpect(jsonPath("$.valor").value(27.80))
            .andExpect(jsonPath("$.pixCopiaCola").value(startsWith("PIXSIMULADO")))
            .andExpect(jsonPath("$.pixExpiraEm").exists())
            .andExpect(jsonPath("$.bandeira").value(nullValue()));

        // Ninguém pagou ainda: o pedido continua aguardando
        mockMvc.perform(get("/api/pedidos/" + pedidoDaAna).with(logadoComo(ANA)))
            .andExpect(jsonPath("$.status").value("AGUARDANDO_PAGAMENTO"));
    }

    @Test
    void gerarPixDuasVezesDevolveOMesmoCodigo() throws Exception {
        String primeiro = JsonPath.read(gerarPix(), "$.pixCopiaCola");
        String segundo = JsonPath.read(gerarPix(), "$.pixCopiaCola");

        assertThat(segundo).isEqualTo(primeiro);
    }

    @Test
    void depoisDeExpirarGeraUmCodigoNovo() throws Exception {
        String antigo = JsonPath.read(gerarPix(), "$.pixCopiaCola");
        expirarPix();
        String novo = JsonPath.read(gerarPix(), "$.pixCopiaCola");

        assertThat(novo).isNotEqualTo(antigo);
    }

    @Test
    void naoPodeGerarPixParaPedidoDeOutroUsuario() throws Exception {
        mockMvc.perform(post(urlPix()).with(logadoComo(BRUNO)))
            .andExpect(status().isNotFound());
    }

    // ---------- confirmar (simulação do banco) ----------

    @Test
    void confirmarPixAprovaEMarcaOPedidoComoPago() throws Exception {
        gerarPix();

        mockMvc.perform(post(urlPix() + "/confirmar").with(logadoComo(ANA)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("APROVADO"));

        mockMvc.perform(get("/api/pedidos/" + pedidoDaAna).with(logadoComo(ANA)))
            .andExpect(jsonPath("$.status").value("PAGO"));
    }

    @Test
    void confirmarSemPixGeradoDeveRetornar422() throws Exception {
        mockMvc.perform(post(urlPix() + "/confirmar").with(logadoComo(ANA)))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.mensagem").value("Não há PIX pendente para este pedido."));
    }

    @Test
    void confirmarPixExpiradoDeveRetornar422() throws Exception {
        gerarPix();
        expirarPix();

        mockMvc.perform(post(urlPix() + "/confirmar").with(logadoComo(ANA)))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.mensagem").value("PIX expirado. Gere um novo código."));
    }

    @Test
    void naoPodeGerarPixDePedidoJaPago() throws Exception {
        gerarPix();
        mockMvc.perform(post(urlPix() + "/confirmar").with(logadoComo(ANA)));

        mockMvc.perform(post(urlPix()).with(logadoComo(ANA)))
            .andExpect(status().isUnprocessableEntity());
    }

    // ---------- linha reaproveitada (UNIQUE pedido_id) ----------

    @Test
    void pagarComCartaoDepoisDoPixLimpaOsCamposDoPix() throws Exception {
        gerarPix();

        String validade = YearMonth.now().plusYears(2).format(DateTimeFormatter.ofPattern("MM/yy"));
        String cartao = """
            {"numero":"4111 1111 1111 1111","nomeTitular":"Ana Teste","validade":"%s","cvv":"123","forma":"CARTAO_CREDITO"}
            """.formatted(validade);

        mockMvc.perform(post("/api/pedidos/" + pedidoDaAna + "/pagamento/cartao").with(logadoComo(ANA))
                .contentType(MediaType.APPLICATION_JSON).content(cartao))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("APROVADO"))
            .andExpect(jsonPath("$.bandeira").value("VISA"))
            .andExpect(jsonPath("$.pixCopiaCola").value(nullValue()))
            .andExpect(jsonPath("$.pixExpiraEm").value(nullValue()));
    }

    // ---------- noRollbackFor: o EXPIRADO precisa ficar gravado ----------

    /**
     * Roda SEM a transação do teste: só assim o commit/rollback do service acontece de verdade.
     * (Com @Transactional na classe, a mudança ficaria só na memória e o teste passaria
     * mesmo SEM o noRollbackFor: um falso positivo.)
     * Por isso limpa manualmente tudo o que o @BeforeEach gravou.
     */
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void pixExpiradoFicaGravadoComoExpiradoMesmoComErro() throws Exception {
        try {
            gerarPix();
            expirarPix();

            mockMvc.perform(post(urlPix() + "/confirmar").with(logadoComo(ANA)))
                .andExpect(status().isUnprocessableEntity());

            // Leitura nova do banco: sem o noRollbackFor, aqui viria PENDENTE
            Pagamento doBanco = pagamentoRepository.findByPedidoId(pedidoDaAna).orElseThrow();
            assertThat(doBanco.getStatus()).isEqualTo(StatusPagamento.EXPIRADO);
        } finally {
            pagamentoRepository.findByPedidoId(pedidoDaAna).ifPresent(pagamentoRepository::delete);
            pedidoRepository.deleteById(pedidoDaAna);

            Produto p = produtoRepository.findById(baunilha.getId()).orElseThrow();
            p.setEstoque(p.getEstoque() + QTD_NO_PEDIDO);   // devolve o estoque baixado pelo pedido
            produtoRepository.save(p);

            enderecoRepository.deleteById(endereco.getId());
            usuarioRepository.deleteById(ana.getId());
            usuarioRepository.deleteById(bruno.getId());
        }
    }
}
