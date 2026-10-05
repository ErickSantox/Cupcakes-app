package br.com.ericksantos.cupcakes.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(min = 2, message = "O Nome deve ter no mínimo 2 caracteres.")
    @Column(nullable = false,length = 100)
    private String nome;

    @Size(min = 2, message = "O sobrenome deve ter no mínimo 2 caracteres.")
    @Column(nullable = false, length = 100)
    private String sobrenome;

    @Column(nullable = false, length = 11)
    private String telefone;

    @Email(message = "Por favor, insira um e-mail válido.")
    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 100)
    private String senhaHash;

    @Column(nullable = false, length = 20)
    private String perfil;

    @Column(nullable = false)
    private Integer tentativasFalhas;

    private LocalDateTime bloqueadoAte;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime atualizadoEm;

}
