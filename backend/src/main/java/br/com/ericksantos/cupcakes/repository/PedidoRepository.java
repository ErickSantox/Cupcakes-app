package br.com.ericksantos.cupcakes.repository;

import br.com.ericksantos.cupcakes.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoRepository extends JpaRepository<Pedido,Long> {
}
