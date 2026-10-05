package br.com.ericksantos.cupcakes.repository;

import br.com.ericksantos.cupcakes.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
}
