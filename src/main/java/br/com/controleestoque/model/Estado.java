package br.com.controleestoque.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "estado")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Estado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estado")
    @EqualsAndHashCode.Include
    private Integer idEstado;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, length = 2)
    private String uf;
}