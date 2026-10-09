package br.com.controleestoque.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "produto")
public class Produto {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_produto")
    private Integer idProduto;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(name = "preco_venda", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoVenda;

    @Column(name = "quantidade_estoque", nullable = false)
    private Integer quantidadeEstoque = 0; // Valor default inicial

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria_produto", nullable = false)
    private CategoriaProduto categoriaProduto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_produto", nullable = false)
    private TipoProduto tipoProduto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_unidade_medida", nullable = false)
    private UnidadeMedida unidadeMedida;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_fornecedor")
    private Fornecedor fornecedor;

    @Column(nullable = false)
    private Boolean ativo = true;
}
