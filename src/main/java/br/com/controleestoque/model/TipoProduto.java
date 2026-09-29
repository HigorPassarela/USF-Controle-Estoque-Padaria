package br.com.controleestoque.model;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "tipo_produto")
public class TipoProduto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_produto")
    private Integer idTipoProduto;

    @Column(nullable = false, length = 100)
    private String descricao;

    public TipoProduto() {}

    public Integer getIdTipoProduto() {
        return idTipoProduto;
    }

    public void setIdTipoProduto(Integer idTipoProduto) {
        this.idTipoProduto = idTipoProduto;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TipoProduto that = (TipoProduto) o;
        return Objects.equals(idTipoProduto, that.idTipoProduto);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idTipoProduto);
    }
}
