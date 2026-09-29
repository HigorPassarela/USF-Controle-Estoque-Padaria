package br.com.controleestoque.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "unidade_medida")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UnidadeMedida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_unidade_medida")
    @EqualsAndHashCode.Include
    private Integer idUnidadeMedida;

    @Column(nullable = false, length = 100)
    private String descricao;

    @Column(nullable = false, length = 10)
    private String sigla;
}