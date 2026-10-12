package br.com.ericksantos.cupcakes.service;


import br.com.ericksantos.cupcakes.dto.CadastroRequest;
import br.com.ericksantos.cupcakes.dto.UsuarioResponse;
import br.com.ericksantos.cupcakes.exception.RegraNegocioException;
import br.com.ericksantos.cupcakes.model.Usuario;
import br.com.ericksantos.cupcakes.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

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
}
