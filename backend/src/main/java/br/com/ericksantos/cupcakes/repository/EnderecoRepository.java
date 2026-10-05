package br.com.ericksantos.cupcakes.repository;

import br.com.ericksantos.cupcakes.model.Endereco;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnderecoRepository  extends JpaRepository<Endereco, Long> {
}
