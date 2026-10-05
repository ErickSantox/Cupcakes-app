package br.com.ericksantos.cupcakes.repository;

import br.com.ericksantos.cupcakes.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario,Long> {
}
