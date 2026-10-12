package br.com.ericksantos.cupcakes.repository;

import br.com.ericksantos.cupcakes.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario,Long> {

    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);
}
