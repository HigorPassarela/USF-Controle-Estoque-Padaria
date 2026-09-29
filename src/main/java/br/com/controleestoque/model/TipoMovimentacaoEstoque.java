package br.com.controleestoque.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tipo_movimentacao_estoque")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TipoMovimentacaoEstoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_movimentacao")
    @EqualsAndHashCode.Include
    private Integer idTipoMovimentacao;

    @Column(nullable = false, length = 100)
    private String descricao;

    @Column(nullable = false, length = 50)
    private String tipo;
}