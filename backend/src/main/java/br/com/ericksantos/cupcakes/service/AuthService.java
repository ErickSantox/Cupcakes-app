package br.com.ericksantos.cupcakes.service;


import br.com.ericksantos.cupcakes.dto.CadastroRequest;
import br.com.ericksantos.cupcakes.dto.LoginRequest;
import br.com.ericksantos.cupcakes.dto.LoginResponse;
import br.com.ericksantos.cupcakes.dto.UsuarioResponse;
import br.com.ericksantos.cupcakes.exception.ContaBloqueadaException;
import br.com.ericksantos.cupcakes.exception.CredenciaisInvalidasException;
import br.com.ericksantos.cupcakes.exception.NaoEncontradoException;
import br.com.ericksantos.cupcakes.exception.RegraNegocioException;
import br.com.ericksantos.cupcakes.model.Usuario;
import br.com.ericksantos.cupcakes.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private static final int MAX_TENTATIVAS = 5;

    @Transactional
    public UsuarioResponse cadastrar(CadastroRequest req) {
        String email = req.email().trim().toLowerCase();

        if (usuarioRepository.existsByEmail(email)) {
            throw new RegraNegocioException("Este e-mail já está cadastrado.");
        }

        Usuario usuario = new Usuario();


        usuario.setNome(req.nome());
        usuario.setSobrenome(req.sobrenome());
        usuario.setTelefone(req.telefone());
        usuario.setEmail(email);
        usuario.setSenhaHash(passwordEncoder.encode(req.senha()));

        Usuario salvo = usuarioRepository.save(usuario);
        log.info("[E-MAIL SIMULADO] Confirmação de cadastro enviada para {}", salvo.getEmail());
        return UsuarioResponse.de(salvo);
    }

    @Transactional(noRollbackFor = {CredenciaisInvalidasException.class, ContaBloqueadaException.class})
    public LoginResponse login(LoginRequest req) {
        String email = req.email().trim().toLowerCase();

        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(CredenciaisInvalidasException::new);

        LocalDateTime agora = LocalDateTime.now();

        if (usuario.getBloqueadoAte() != null && usuario.getBloqueadoAte().isAfter(agora)) {
            throw new ContaBloqueadaException();
        }

        if (!passwordEncoder.matches(req.senha(), usuario.getSenhaHash())) {
            int tentativas = usuario.getTentativasFalhas() + 1;
            if (tentativas >= MAX_TENTATIVAS) {
                usuario.setTentativasFalhas(0);
                usuario.setBloqueadoAte(agora.plusMinutes(15));
                usuarioRepository.save(usuario);
                throw new ContaBloqueadaException();
            }
            usuario.setTentativasFalhas(tentativas);
            usuarioRepository.save(usuario);
            throw new CredenciaisInvalidasException();
        }

        usuario.setTentativasFalhas(0);
        usuario.setBloqueadoAte(null);
        usuarioRepository.save(usuario);

        TokenService.TokenGerado token = tokenService.gerar(usuario);
        return new LoginResponse(token.token(), token.expiraEm(), UsuarioResponse.de(usuario));
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
            .map(UsuarioResponse::de)
            .orElseThrow(() -> new NaoEncontradoException("Usuário não encontrado."));
    }
}
