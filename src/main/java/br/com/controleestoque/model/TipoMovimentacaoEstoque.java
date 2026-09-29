package br.com.controleestoque.model;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "tipo_movimentacao_estoque")
public class TipoMovimentacaoEstoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_movimentacao")
    private Integer idTipoMovimentacao;

    @Column(nullable = false, length = 100)
    private String descricao;

    @Column(nullable = false, length = 50)
    private String tipo;

    public TipoMovimentacaoEstoque() {}

    public Integer getIdTipoMovimentacao() {
        return idTipoMovimentacao;
    }

    public void setIdTipoMovimentacao(Integer idTipoMovimentacao) {
        this.idTipoMovimentacao = idTipoMovimentacao;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TipoMovimentacaoEstoque that = (TipoMovimentacaoEstoque) o;
        return Objects.equals(idTipoMovimentacao, that.idTipoMovimentacao);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idTipoMovimentacao);
    }
}
