package br.com.ericksantos.cupcakes.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import br.com.ericksantos.cupcakes.model.enums.Perfil;
import br.com.ericksantos.cupcakes.repository.UsuarioRepository;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

    private static final String URL = "/api/auth/cadastro";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // US-01 RN#2 + CA#1: cadastro válido retorna 201 e nunca expõe a senha
    @Test
    void deveCadastrarUsuarioValido() throws Exception {
        cadastrar("erick@teste.com", "senha123")
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.email").value("erick@teste.com"))
            .andExpect(jsonPath("$.perfil").value("CLIENTE"))
            .andExpect(jsonPath("$.senha").doesNotExist())
            .andExpect(jsonPath("$.senhaHash").doesNotExist());
    }

    // US-01 CA#2: senha com menos de 8 caracteres é recusada com 400
    @Test
    void deveRecusarSenhaCurta() throws Exception {
        cadastrar("curta@teste.com", "12345")
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.campos.senha").exists());
    }

    // US-01 RN#1: e-mail não pode ser reutilizável (inclusive com maiúsculas)
    @Test
    void deveRecusarEmailDuplicado() throws Exception {
        cadastrar("duplicado@teste.com", "senha123")
            .andExpect(status().isCreated());

        cadastrar("Duplicado@Teste.com", "outrasenha")
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.mensagem").value("Este e-mail já está cadastrado."));
    }

    // Segurança: a senha é gravada com hash BCrypt, nunca pura
    @Test
    void deveGravarSenhaComHash() throws Exception {
        cadastrar("hash@teste.com", "senha123")
            .andExpect(status().isCreated());

        Usuario usuario = usuarioRepository.findByEmail("hash@teste.com").orElseThrow();

        assertThat(usuario.getSenhaHash()).isNotEqualTo("senha123");
        assertThat(passwordEncoder.matches("senha123", usuario.getSenhaHash())).isTrue();
        assertThat(usuario.getPerfil()).isEqualTo(Perfil.CLIENTE);
        assertThat(usuario.getTentativasFalhas()).isZero();
    }

    private ResultActions cadastrar(String email, String senha) throws Exception {
        return mockMvc.perform(post(URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"nome":"Erick","sobrenome":"Santos","telefone":"11999998888",
                 "email":"%s","senha":"%s"}
                """.formatted(email, senha)));
    }
}
