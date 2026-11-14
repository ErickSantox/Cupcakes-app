package br.com.ericksantos.cupcakes.service;

import br.com.ericksantos.cupcakes.exception.RegraNegocioException;
import br.com.ericksantos.cupcakes.model.TokenRedefinicaoSenha;
import br.com.ericksantos.cupcakes.model.Usuario;
import br.com.ericksantos.cupcakes.repository.TokenRedefinicaoSenhaRepository;
import br.com.ericksantos.cupcakes.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

/**
 * US-02 CA#3: recuperação de senha por link de uso único, válido por 15 minutos.
 * Sem servidor SMTP no escopo, o "e-mail" com o link é simulado em log.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RecuperacaoSenhaService {

    private static final int VALIDADE_MINUTOS = 15;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UsuarioRepository usuarioRepository;
    private final TokenRedefinicaoSenhaRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    /**
     * Gera o link se o e-mail existir. Para quem chama a API, a resposta é sempre a mesma,
     * para não revelar quais e-mails estão cadastrados. Devolve o token só para os testes.
     */
    @Transactional
    public Optional<String> solicitar(String email) {
        Optional<Usuario> usuario = usuarioRepository.findByEmail(email.trim().toLowerCase());
        if (usuario.isEmpty()) {
            log.info("[E-MAIL SIMULADO] Redefinição pedida para e-mail não cadastrado; nada foi enviado.");
            return Optional.empty();
        }

        LocalDateTime agora = LocalDateTime.now();
        // Um link novo invalida os anteriores ainda não usados
        tokenRepository.findByUsuarioAndUsadoEmIsNull(usuario.get()).forEach(t -> t.setUsadoEm(agora));

        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        TokenRedefinicaoSenha registro = new TokenRedefinicaoSenha();
        registro.setUsuario(usuario.get());
        registro.setTokenHash(hash(token));   // o banco guarda só o hash, nunca o token
        registro.setExpiraEm(agora.plusMinutes(VALIDADE_MINUTOS));
        tokenRepository.save(registro);

        log.info("[E-MAIL SIMULADO] Link de redefinição de senha para {}: {}/redefinir-senha?token={}",
            usuario.get().getEmail(), frontendUrl, token);
        return Optional.of(token);
    }

    @Transactional
    public void redefinir(String token, String novaSenha) {
        TokenRedefinicaoSenha registro = tokenRepository.findByTokenHash(hash(token))
            .orElseThrow(() -> new RegraNegocioException("Link de redefinição inválido ou expirado."));

        LocalDateTime agora = LocalDateTime.now();
        if (registro.getUsadoEm() != null || registro.getExpiraEm().isBefore(agora)) {
            throw new RegraNegocioException("Link de redefinição inválido ou expirado.");
        }

        Usuario usuario = registro.getUsuario();
        usuario.setSenhaHash(passwordEncoder.encode(novaSenha));
        usuario.setTentativasFalhas(0);   // quem redefiniu a senha não continua bloqueado
        usuario.setBloqueadoAte(null);
        registro.setUsadoEm(agora);
    }

    private static String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível", e);
        }
    }
}
