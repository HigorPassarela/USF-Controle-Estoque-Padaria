package br.com.controleestoque.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "lote")
public class Lote {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_lote")
    private Integer idLote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_produto", nullable = false)
    private Produto produto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_fornecedor")
    private Fornecedor fornecedor;

    @Column(name = "numero_lote", length = 50)
    private String numeroLote;

    @Column(name = "data_entrada", nullable = false)
    private Instant dataEntrada;

    /** Sem validade, o lote nunca vence. */
    @Column(name = "data_validade")
    private LocalDate dataValidade;

    @Column(name = "quantidade_inicial", nullable = false, precision = 10, scale = 3)
    private BigDecimal quantidadeInicial;

    /** Quanto ainda resta do lote. */
    @Column(name = "quantidade_atual", nullable = false, precision = 10, scale = 3)
    private BigDecimal quantidadeAtual;

    @Column(name = "custo_unitario", precision = 10, scale = 2)
    private BigDecimal custoUnitario;

    /** Vencido é o lote cuja validade ficou antes de hoje; no próprio dia da validade ainda vale. */
    public boolean vencidoEm(LocalDate hoje) {
        return dataValidade != null && dataValidade.isBefore(hoje);
    }
}
