package br.com.ericksantos.cupcakes.repository;

import br.com.ericksantos.cupcakes.model.Pedido;
import br.com.ericksantos.cupcakes.model.enums.StatusPedido;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido,Long> {

    @Query(value = "SELECT nextval('pedido_numero_seq')",
    nativeQuery = true)
    Long proximoNumero();

    Optional<Pedido> findByIdAndUsuarioEmail(Long id, String email);

    @Query("""
        SELECT p FROM Pedido p
        WHERE (:status IS NULL OR p.status = :status)
          AND p.criadoEm >= :inicio
          AND p.criadoEm < :fim
        """)
    Page<Pedido> filtrar(@Param("status") StatusPedido status,
                         @Param("inicio") LocalDateTime inicio,
                         @Param("fim") LocalDateTime fim,
                         Pageable pageable);

    List<Pedido> findByUsuarioEmailOrderByCriadoEmDesc(String email);
}
