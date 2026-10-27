package br.com.ericksantos.cupcakes.repository;

import br.com.ericksantos.cupcakes.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido,Long> {

    @Query(value = "SELECT nextval('pedido_numero_seq')",
    nativeQuery = true)
    Long proximoNumero();

    Optional<Pedido> findByIdAndUsuarioEmail(Long id, String email);


}
