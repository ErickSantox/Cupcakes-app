package br.com.ericksantos.cupcakes.config;

import br.com.ericksantos.cupcakes.model.Usuario;
import br.com.ericksantos.cupcakes.model.enums.Perfil;
import br.com.ericksantos.cupcakes.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String email;

    @Value("${app.admin.senha}")
    private String senha;

    @Override
    public void run(String ... args){
        if (email.isBlank() || senha.isBlank()){
            log.warn("ADMIN_EMAIL/ADMIN_SENHA não configurados: nenhum adminstrador foi criado.");
            return;
        }

        String emailNormalizado = email.trim().toLowerCase();

        if (usuarioRepository.existsByEmail(emailNormalizado)){
            return;
        }

        Usuario admin = new Usuario();
        admin.setNome("Admin");
        admin.setSobrenome("Cupcakes");
        admin.setTelefone("00000000000");
        admin.setEmail(emailNormalizado);
        admin.setSenhaHash(passwordEncoder.encode(senha));
        admin.setPerfil(Perfil.ADMIN);

        usuarioRepository.save(admin);
        log.info("Adminstrador {} criado.", emailNormalizado);

    }


}
