package br.com.ericksantos.cupcakes.repository;

import br.com.ericksantos.cupcakes.model.ItemPedido;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemPedidoRepository extends JpaRepository<ItemPedido,Long> {
}
