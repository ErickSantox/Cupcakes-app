package br.com.ericksantos.cupcakes.model;


import br.com.ericksantos.cupcakes.model.enums.StatusPedido;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pedido")
@Getter
@Setter
@NoArgsConstructor
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(nullable = false, length = 20, unique = true)
    private String numero;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal frete;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private StatusPedido status;

    @Column(nullable = false,length = 100)
    private String entregaLogradouro;

    @Column(nullable = false, length = 10)
    private String entregaNumero;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(nullable = false, length = 8)
    private String entregaCep;

    @Column( length = 50)
    private String entregaComplemento;

    @Column(nullable = false, length = 50)
    private String entregaBairro;

    @Column(nullable = false, length = 50)
    private String entregaCidade;

    @Column(nullable = false, length = 2)
    private String entregaUf;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime atualizadoEm;

}
