package br.com.ericksantos.cupcakes.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

import br.com.ericksantos.cupcakes.dto.PagamentoCartaoRequest;
import br.com.ericksantos.cupcakes.model.Endereco;
import br.com.ericksantos.cupcakes.model.Produto;
import br.com.ericksantos.cupcakes.model.Usuario;
import br.com.ericksantos.cupcakes.model.enums.FormaPagamento;
import br.com.ericksantos.cupcakes.repository.EnderecoRepository;
import br.com.ericksantos.cupcakes.repository.PagamentoRepository;
import br.com.ericksantos.cupcakes.repository.ProdutoRepository;
import br.com.ericksantos.cupcakes.repository.UsuarioRepository;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class PagamentoControllerTest {

    private static final String ANA = "ana@teste.com";
    private static final String BRUNO = "bruno@teste.com";

    private static final String VISA_APROVADO = "4111 1111 1111 1111";
    private static final String VISA_RECUSADO = "4000 0000 0000 0002";   // regra do gateway simulado
    private static final String CVV = "123";

    /** Validade relativa a hoje: o teste não envelhece */
    private static final String VALIDADE_OK =
        YearMonth.now().plusYears(2).format(DateTimeFormatter.ofPattern("MM/yy"));

    @Autowired private MockMvc mockMvc;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private EnderecoRepository enderecoRepository;
    @Autowired private ProdutoRepository produtoRepository;
    @Autowired private PagamentoRepository pagamentoRepository;

    private Long pedidoDaAna;

    @BeforeEach
    void setUp() throws Exception {
        Usuario ana = criarUsuario("Ana", ANA);
        criarUsuario("Bruno", BRUNO);
        Endereco endereco = criarEndereco(ana);
        pedidoDaAna = criarPedido(ANA, endereco.getId());
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

    /** CEP de SP: frete R$ 8,00 */
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

    /** 2 × Baunilha (R$ 9,90) + frete 8,00 = total R$ 27,80 */
    private Long criarPedido(String email, Long enderecoId) throws Exception {
        Produto baunilha = produtoRepository.findAll().stream()
            .filter(p -> p.getNome().equals("Baunilha Clássico"))
            .findFirst().orElseThrow();

        String corpo = """
            {"enderecoId":%d,"itens":[{"produtoId":%d,"quantidade":2}]}
            """.formatted(enderecoId, baunilha.getId());

        String resposta = mockMvc.perform(post("/api/pedidos").with(logadoComo(email))
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(resposta, "$.id")).longValue();
    }

    private static RequestPostProcessor logadoComo(String email) {
        return jwt().jwt(j -> j.subject(email));
    }

    private static String cartao(String numero, String forma) {
        return """
            {"numero":"%s","nomeTitular":"Ana Teste","validade":"%s","cvv":"%s","forma":"%s"}
            """.formatted(numero, VALIDADE_OK, CVV, forma);
    }

    private String urlPagamento(Long pedidoId) {
        return "/api/pedidos/" + pedidoId + "/pagamento/cartao";
    }

    // ---------- segurança ----------

    @Test
    void semTokenDeveRetornar401() throws Exception {
        mockMvc.perform(post(urlPagamento(pedidoDaAna))
                .contentType(MediaType.APPLICATION_JSON).content(cartao(VISA_APROVADO, "CARTAO_CREDITO")))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void naoPodePagarPedidoDeOutroUsuario() throws Exception {
        mockMvc.perform(post(urlPagamento(pedidoDaAna)).with(logadoComo(BRUNO))
                .contentType(MediaType.APPLICATION_JSON).content(cartao(VISA_APROVADO, "CARTAO_CREDITO")))
            .andExpect(status().isNotFound());
    }

    // ---------- CA#1 e CA#2: aprovação e recusa ----------

    @Test
    void pagamentoAprovadoDeveMarcarOPedidoComoPago() throws Exception {
        mockMvc.perform(post(urlPagamento(pedidoDaAna)).with(logadoComo(ANA))
                .contentType(MediaType.APPLICATION_JSON).content(cartao(VISA_APROVADO, "CARTAO_CREDITO")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("APROVADO"))
            .andExpect(jsonPath("$.bandeira").value("VISA"))
            .andExpect(jsonPath("$.ultimosDigitos").value("1111"))
            .andExpect(jsonPath("$.valor").value(27.80))       // valor vem do pedido, não do request
            .andExpect(jsonPath("$.motivo").value(nullValue()));

        mockMvc.perform(get("/api/pedidos/" + pedidoDaAna).with(logadoComo(ANA)))
            .andExpect(jsonPath("$.status").value("PAGO"));
    }

    @Test
    void pagamentoRecusadoEGravadoEOPedidoContinuaAguardando() throws Exception {
        mockMvc.perform(post(urlPagamento(pedidoDaAna)).with(logadoComo(ANA))
                .contentType(MediaType.APPLICATION_JSON).content(cartao(VISA_RECUSADO, "CARTAO_CREDITO")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("RECUSADO"))
            .andExpect(jsonPath("$.motivo").value("Transação não autorizada pelo emissor."));

        // A recusa NÃO é rollback: a tentativa fica registrada
        assertThat(pagamentoRepository.findByPedidoId(pedidoDaAna)).isPresent();

        mockMvc.perform(get("/api/pedidos/" + pedidoDaAna).with(logadoComo(ANA)))
            .andExpect(jsonPath("$.status").value("AGUARDANDO_PAGAMENTO"));
    }

    @Test
    void novaTentativaDepoisDaRecusaReaproveitaOMesmoPagamento() throws Exception {
        String recusado = mockMvc.perform(post(urlPagamento(pedidoDaAna)).with(logadoComo(ANA))
                .contentType(MediaType.APPLICATION_JSON).content(cartao(VISA_RECUSADO, "CARTAO_CREDITO")))
            .andReturn().getResponse().getContentAsString();
        Number idRecusado = JsonPath.read(recusado, "$.id");

        // Sem reaproveitar a linha, a constraint UNIQUE(pedido_id) estouraria aqui
        mockMvc.perform(post(urlPagamento(pedidoDaAna)).with(logadoComo(ANA))
                .contentType(MediaType.APPLICATION_JSON).content(cartao(VISA_APROVADO, "CARTAO_DEBITO")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(idRecusado.longValue()))
            .andExpect(jsonPath("$.status").value("APROVADO"))
            .andExpect(jsonPath("$.forma").value("CARTAO_DEBITO"));
    }

    // ---------- regras (422) ----------

    @Test
    void naoPodePagarDuasVezes() throws Exception {
        mockMvc.perform(post(urlPagamento(pedidoDaAna)).with(logadoComo(ANA))
                .contentType(MediaType.APPLICATION_JSON).content(cartao(VISA_APROVADO, "CARTAO_CREDITO")))
            .andExpect(status().isOk());

        mockMvc.perform(post(urlPagamento(pedidoDaAna)).with(logadoComo(ANA))
                .contentType(MediaType.APPLICATION_JSON).content(cartao(VISA_APROVADO, "CARTAO_CREDITO")))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.mensagem").value("Este pedido não está aguardando pagamento."));
    }

    @Test
    void pixNoEndpointDeCartaoDeveRetornar422() throws Exception {
        mockMvc.perform(post(urlPagamento(pedidoDaAna)).with(logadoComo(ANA))
                .contentType(MediaType.APPLICATION_JSON).content(cartao(VISA_APROVADO, "PIX")))
            .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void cartaoInvalidoNaoGravaPagamento() throws Exception {
        mockMvc.perform(post(urlPagamento(pedidoDaAna)).with(logadoComo(ANA))
                .contentType(MediaType.APPLICATION_JSON).content(cartao("4111 1111 1111 1112", "CARTAO_CREDITO")))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.mensagem").value("Número de cartão inválido."));

        assertThat(pagamentoRepository.findByPedidoId(pedidoDaAna)).isEmpty();
    }

    // ---------- validação (400) ----------

    @Test
    void semCvvDeveRetornar400() throws Exception {
        String semCvv = """
            {"numero":"%s","nomeTitular":"Ana Teste","validade":"%s","forma":"CARTAO_CREDITO"}
            """.formatted(VISA_APROVADO, VALIDADE_OK);

        mockMvc.perform(post(urlPagamento(pedidoDaAna)).with(logadoComo(ANA))
                .contentType(MediaType.APPLICATION_JSON).content(semCvv))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.campos.cvv").exists());
    }

    // ---------- 🔒 CA#3: dados do cartão não vazam ----------

    @Test
    void respostaNaoExpoeNumeroCompletoNemCvv() throws Exception {
        String resposta = mockMvc.perform(post(urlPagamento(pedidoDaAna)).with(logadoComo(ANA))
                .contentType(MediaType.APPLICATION_JSON).content(cartao(VISA_APROVADO, "CARTAO_CREDITO")))
            .andReturn().getResponse().getContentAsString();

        assertThat(resposta)
            .doesNotContain("4111111111111111")
            .doesNotContain("4111 1111 1111 1111")
            .doesNotContain("cvv");
    }

    @Test
    void toStringDoRequestMascaraNumeroECvv() {
        var req = new PagamentoCartaoRequest("4111111111111111", "Ana Teste", "12/30", "987",
            FormaPagamento.CARTAO_CREDITO);

        assertThat(req.toString())
            .contains("************1111")
            .doesNotContain("4111111111111111")
            .doesNotContain("987");
    }
}
