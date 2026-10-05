package br.com.ericksantos.cupcakes.repository;

import br.com.ericksantos.cupcakes.model.Pagamento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PagamentoRepository extends JpaRepository<Pagamento,Long> {
}
