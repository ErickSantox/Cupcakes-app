package br.com.ericksantos.cupcakes.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

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

import br.com.ericksantos.cupcakes.model.Usuario;
import br.com.ericksantos.cupcakes.repository.TokenRedefinicaoSenhaRepository;
import br.com.ericksantos.cupcakes.repository.UsuarioRepository;
import br.com.ericksantos.cupcakes.service.RecuperacaoSenhaService;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class RecuperacaoSenhaTest {

    private static final String EMAIL = "recupera@teste.com";

    @Autowired private MockMvc mockMvc;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private TokenRedefinicaoSenhaRepository tokenRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private RecuperacaoSenhaService recuperacaoSenhaService;

    private Usuario usuario;

    @BeforeEach
    void criarUsuario() {
        Usuario u = new Usuario();
        u.setNome("Erick");
        u.setSobrenome("Santos");
        u.setTelefone("11999998888");
        u.setEmail(EMAIL);
        u.setSenhaHash(passwordEncoder.encode("senhaAntiga1"));
        usuario = usuarioRepository.save(u);
    }

    // ---------- helpers ----------

    private ResultActions esqueci(String email) throws Exception {
        return mockMvc.perform(post("/api/auth/esqueci-senha")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"" + email + "\"}"));
    }

    private ResultActions redefinir(String token, String novaSenha) throws Exception {
        return mockMvc.perform(post("/api/auth/redefinir-senha")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"token\":\"" + token + "\",\"novaSenha\":\"" + novaSenha + "\"}"));
    }

    private ResultActions login(String senha) throws Exception {
        return mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"" + EMAIL + "\",\"senha\":\"" + senha + "\"}"));
    }

    private String gerarToken() {
        return recuperacaoSenhaService.solicitar(EMAIL).orElseThrow();
    }

    // ---------- solicitar ----------

    @Test
    void emailCadastradoGeraTokenComHash() throws Exception {
        esqueci(EMAIL).andExpect(status().isNoContent());

        assertThat(tokenRepository.findByUsuarioAndUsadoEmIsNull(usuario)).hasSize(1);
    }

    // Segurança: e-mail inexistente recebe a MESMA resposta (não revela quem tem conta)
    @Test
    void emailNaoCadastradoTambemDevolve204() throws Exception {
        esqueci("ninguem@teste.com").andExpect(status().isNoContent());

        assertThat(tokenRepository.count()).isZero();
    }

    @Test
    void emailInvalidoDeveRetornar400() throws Exception {
        esqueci("nao-e-email").andExpect(status().isBadRequest());
    }

    @Test
    void novoLinkInvalidaOAnterior() throws Exception {
        String primeiro = gerarToken();
        gerarToken();

        redefinir(primeiro, "senhaNova123").andExpect(status().isUnprocessableEntity());
    }

    // ---------- redefinir ----------

    @Test
    void tokenValidoTrocaASenha() throws Exception {
        String token = gerarToken();

        redefinir(token, "senhaNova123").andExpect(status().isNoContent());

        login("senhaNova123").andExpect(status().isOk());
        login("senhaAntiga1").andExpect(status().isUnauthorized());
    }

    // US-02 CA#3: uso único
    @Test
    void tokenNaoPodeSerUsadoDuasVezes() throws Exception {
        String token = gerarToken();
        redefinir(token, "senhaNova123").andExpect(status().isNoContent());

        redefinir(token, "outraSenha123")
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.mensagem").value("Link de redefinição inválido ou expirado."));
    }

    // US-02 CA#3: validade de 15 minutos
    @Test
    void tokenExpiradoDeveRetornar422() throws Exception {
        String token = gerarToken();
        tokenRepository.findByUsuarioAndUsadoEmIsNull(usuario)
            .forEach(t -> t.setExpiraEm(LocalDateTime.now().minusMinutes(1)));

        redefinir(token, "senhaNova123").andExpect(status().isUnprocessableEntity());
    }

    @Test
    void tokenInexistenteDeveRetornar422() throws Exception {
        redefinir("token-que-nao-existe", "senhaNova123").andExpect(status().isUnprocessableEntity());
    }

    @Test
    void senhaCurtaDeveRetornar400() throws Exception {
        String token = gerarToken();
        redefinir(token, "123").andExpect(status().isBadRequest());
    }

    @Test
    void redefinirDesbloqueiaAConta() throws Exception {
        usuario.setBloqueadoAte(LocalDateTime.now().plusMinutes(10));
        String token = gerarToken();

        redefinir(token, "senhaNova123").andExpect(status().isNoContent());

        login("senhaNova123").andExpect(status().isOk());
    }
}
