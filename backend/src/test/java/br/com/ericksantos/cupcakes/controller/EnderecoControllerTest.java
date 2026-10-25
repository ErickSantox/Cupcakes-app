package br.com.ericksantos.cupcakes.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

import br.com.ericksantos.cupcakes.model.Endereco;
import br.com.ericksantos.cupcakes.model.Usuario;
import br.com.ericksantos.cupcakes.repository.EnderecoRepository;
import br.com.ericksantos.cupcakes.repository.UsuarioRepository;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class EnderecoControllerTest {

    private static final String ANA = "ana@teste.com";
    private static final String BRUNO = "bruno@teste.com";

    @Autowired private MockMvc mockMvc;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private EnderecoRepository enderecoRepository;

    private Usuario ana;
    private Usuario bruno;

    @BeforeEach
    void setUp() {
        ana = criarUsuario("Ana", ANA);
        bruno = criarUsuario("Bruno", BRUNO);
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

    private Endereco criarEndereco(Usuario dono, boolean padrao) {
        Endereco e = new Endereco();
        e.setUsuario(dono);
        e.setCep("01310100");
        e.setLogradouro("Av. Paulista");
        e.setNumero("1000");
        e.setBairro("Bela Vista");
        e.setCidade("São Paulo");
        e.setUf("SP");
        e.setPadrao(padrao);
        return enderecoRepository.save(e);
    }

    /** Simula o usuário logado: o "subject" do token é o e-mail */
    private static RequestPostProcessor logadoComo(String email) {
        return jwt().jwt(j -> j.subject(email));
    }

    private String json(String cep, boolean padrao) {
        return """
            {"cep":"%s","logradouro":"Rua das Flores","numero":"10","complemento":"",
             "bairro":"Centro","cidade":"Campinas","uf":"SP","padrao":%s}
            """.formatted(cep, padrao);
    }

    // ---------- segurança ----------

    @Test
    void semTokenDeveRetornar401() throws Exception {
        mockMvc.perform(get("/api/enderecos"))
            .andExpect(status().isUnauthorized());
    }

    // ---------- CA#1 e CA#2 ----------

    @Test
    void primeiroEnderecoViraPadraoAutomaticamente() throws Exception {
        mockMvc.perform(post("/api/enderecos").with(logadoComo(ANA))
                .contentType(MediaType.APPLICATION_JSON).content(json("13010000", false)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.padrao").value(true));
    }

    @Test
    void listaSomenteOsEnderecosDoUsuarioLogado() throws Exception {
        criarEndereco(ana, true);
        criarEndereco(bruno, true);

        mockMvc.perform(get("/api/enderecos").with(logadoComo(ANA)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void definirPadraoDesmarcaOAnterior() throws Exception {
        Endereco antigo = criarEndereco(ana, true);
        Endereco novo = criarEndereco(ana, false);

        mockMvc.perform(patch("/api/enderecos/" + novo.getId() + "/padrao").with(logadoComo(ANA)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.padrao").value(true));

        // A lista vem com o padrão primeiro: o novo no topo, o antigo desmarcado
        mockMvc.perform(get("/api/enderecos").with(logadoComo(ANA)))
            .andExpect(jsonPath("$[0].id").value(novo.getId()))
            .andExpect(jsonPath("$[1].id").value(antigo.getId()))
            .andExpect(jsonPath("$[1].padrao").value(false));
    }

    @Test
    void excluirOPadraoPromoveOutro() throws Exception {
        Endereco padrao = criarEndereco(ana, true);
        Endereco outro = criarEndereco(ana, false);

        mockMvc.perform(delete("/api/enderecos/" + padrao.getId()).with(logadoComo(ANA)))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/enderecos").with(logadoComo(ANA)))
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id").value(outro.getId()))
            .andExpect(jsonPath("$[0].padrao").value(true));
    }

    // ---------- RN#1 e CA#3 ----------

    @Test
    void sextoEnderecoDeveRetornar422() throws Exception {
        for (int i = 0; i < 5; i++) {
            criarEndereco(ana, i == 0);
        }
        mockMvc.perform(post("/api/enderecos").with(logadoComo(ANA))
                .contentType(MediaType.APPLICATION_JSON).content(json("13010000", false)))
            .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void cepInvalidoDeveRetornar400() throws Exception {
        mockMvc.perform(post("/api/enderecos").with(logadoComo(ANA))
                .contentType(MediaType.APPLICATION_JSON).content(json("123", false)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.campos.cep").exists());
    }

    // ---------- 🔒 IDOR ----------

    @Test
    void naoPodeEditarEnderecoDeOutroUsuario() throws Exception {
        Endereco daAna = criarEndereco(ana, true);

        mockMvc.perform(put("/api/enderecos/" + daAna.getId()).with(logadoComo(BRUNO))
                .contentType(MediaType.APPLICATION_JSON).content(json("13010000", false)))
            .andExpect(status().isNotFound());
    }

    @Test
    void naoPodeExcluirEnderecoDeOutroUsuario() throws Exception {
        Endereco daAna = criarEndereco(ana, true);

        mockMvc.perform(delete("/api/enderecos/" + daAna.getId()).with(logadoComo(BRUNO)))
            .andExpect(status().isNotFound());

        // E o endereço da Ana continua lá
        mockMvc.perform(get("/api/enderecos").with(logadoComo(ANA)))
            .andExpect(jsonPath("$", hasSize(1)));
    }
}
