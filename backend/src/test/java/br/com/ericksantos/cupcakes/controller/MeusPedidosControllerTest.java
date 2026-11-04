package br.com.ericksantos.cupcakes.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

import br.com.ericksantos.cupcakes.model.Endereco;
import br.com.ericksantos.cupcakes.model.Produto;
import br.com.ericksantos.cupcakes.model.Usuario;
import br.com.ericksantos.cupcakes.repository.EnderecoRepository;
import br.com.ericksantos.cupcakes.repository.ProdutoRepository;
import br.com.ericksantos.cupcakes.repository.UsuarioRepository;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class MeusPedidosControllerTest {

    private static final String ANA = "ana@teste.com";
    private static final String BRUNO = "bruno@teste.com";
    private static final String URL = "/api/pedidos/meus";

    @Autowired private MockMvc mockMvc;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private EnderecoRepository enderecoRepository;
    @Autowired private ProdutoRepository produtoRepository;

    private Endereco enderecoDaAna;
    private Produto baunilha;

    @BeforeEach
    void setUp() {
        Usuario ana = criarUsuario("Ana", ANA);
        criarUsuario("Bruno", BRUNO);
        enderecoDaAna = criarEndereco(ana);
        baunilha = produtoRepository.findAll().stream()
            .filter(p -> p.getNome().equals("Baunilha Clássico"))
            .findFirst().orElseThrow();
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

    private int criarPedidoDaAna() throws Exception {
        String corpo = """
            {"enderecoId":%d,"itens":[{"produtoId":%d,"quantidade":1}]}
            """.formatted(enderecoDaAna.getId(), baunilha.getId());

        String resposta = mockMvc.perform(post("/api/pedidos").with(logadoComo(ANA))
                .contentType(MediaType.APPLICATION_JSON).content(corpo))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(resposta, "$.id")).intValue();
    }

    private static RequestPostProcessor logadoComo(String email) {
        return jwt().jwt(j -> j.subject(email));
    }

    // ---------- testes ----------

    @Test
    void listaOsPedidosDoClienteDoMaisNovoParaOMaisAntigo() throws Exception {
        int primeiro = criarPedidoDaAna();
        int segundo = criarPedidoDaAna();

        mockMvc.perform(get(URL).with(logadoComo(ANA)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].id").value(segundo))
            .andExpect(jsonPath("$[1].id").value(primeiro))
            .andExpect(jsonPath("$[0].status").value("AGUARDANDO_PAGAMENTO"));
    }

    @Test
    void clienteNaoVePedidosDeOutroCliente() throws Exception {
        criarPedidoDaAna();

        mockMvc.perform(get(URL).with(logadoComo(BRUNO)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void rotaMeusNaoEhConfundidaComId() throws Exception {
        // se "/meus" caísse no GET /{id}, daria 400 (conversão de "meus" para Long)
        mockMvc.perform(get(URL).with(logadoComo(BRUNO)))
            .andExpect(status().isOk());
    }

    @Test
    void semTokenDeveRetornar401() throws Exception {
        mockMvc.perform(get(URL)).andExpect(status().isUnauthorized());
    }
}
