package br.com.ericksantos.cupcakes.controller;

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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

import br.com.ericksantos.cupcakes.model.Usuario;
import br.com.ericksantos.cupcakes.repository.UsuarioRepository;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class AuthLoginTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @BeforeEach
    void criarUsuario() {
        Usuario u = new Usuario();
        u.setNome("Erick");
        u.setSobrenome("Santos");
        u.setTelefone("11999998888");
        u.setEmail("login@teste.com");
        u.setSenhaHash(passwordEncoder.encode("senha123"));
        usuarioRepository.save(u);
    }

    // US-02 CA#1: login válido devolve token
    @Test
    void deveLogarComCredenciaisValidas() throws Exception {
        login("login@teste.com", "senha123")
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andExpect(jsonPath("$.expiraEm").exists())
            .andExpect(jsonPath("$.usuario.email").value("login@teste.com"));
    }

    // US-02 CA#2: senha errada → 401 com mensagem genérica
    @Test
    void deveRecusarSenhaErrada() throws Exception {
        login("login@teste.com", "errada123")
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.mensagem").value("E-mail ou senha inválidos."));
    }

    // Segurança: e-mail inexistente dá a MESMA resposta (não revela quem está cadastrado)
    @Test
    void deveRecusarEmailInexistenteComMesmaMensagem() throws Exception {
        login("naoexiste@teste.com", "senha123")
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.mensagem").value("E-mail ou senha inválidos."));
    }

    // US-02 RN#1: bloqueia após 5 tentativas, mesmo com a senha certa depois
    @Test
    void deveBloquearAposCincoTentativas() throws Exception {
        for (int i = 1; i <= 4; i++) {
            login("login@teste.com", "errada123").andExpect(status().isUnauthorized());
        }
        login("login@teste.com", "errada123").andExpect(status().isLocked());
        login("login@teste.com", "senha123").andExpect(status().isLocked());
    }

    // O token dá acesso a rota protegida
    @Test
    void deveAcessarRotaProtegidaComToken() throws Exception {
        String token = extrairToken(login("login@teste.com", "senha123"));

        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("login@teste.com"));
    }

    // Sem token → 401
    @Test
    void deveRecusarRotaProtegidaSemToken() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
            .andExpect(status().isUnauthorized());
    }

    // US-15 RN#1: CLIENTE não acessa rota de admin → 403
    @Test
    void clienteNaoDeveAcessarRotaAdmin() throws Exception {
        String token = extrairToken(login("login@teste.com", "senha123"));

        mockMvc.perform(get("/api/admin/pedidos").header("Authorization", "Bearer " + token))
            .andExpect(status().isForbidden());
    }

    private ResultActions login(String email, String senha) throws Exception {
        return mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"email":"%s","senha":"%s"}
                """.formatted(email, senha)));
    }

    private String extrairToken(ResultActions resultado) throws Exception {
        return JsonPath.read(resultado.andReturn().getResponse().getContentAsString(), "$.token");
    }
}
