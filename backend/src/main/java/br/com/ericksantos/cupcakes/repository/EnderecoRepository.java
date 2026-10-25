package br.com.ericksantos.cupcakes.repository;

import br.com.ericksantos.cupcakes.model.Endereco;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnderecoRepository  extends JpaRepository<Endereco, Long> {

    List<Endereco> findByUsuarioEmailOrderByPadraoDescIdAsc(String email);

    Optional<Endereco> findByIdAndUsuarioEmail(Long id, String email);

    long countByUsuarioEmail(String email);

    Optional<Endereco> findByUsuarioEmailAndPadraoTrue(String email);

    Optional<Endereco> findFirstByUsuarioEmailOrderByIdAsc(String email);
}
