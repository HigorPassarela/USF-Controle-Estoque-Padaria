package br.com.controleestoque.model;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "unidade_medida")
public class UnidadeMedida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_unidade_medida")
    private Integer idUnidadeMedida;

    @Column(nullable = false, length = 100)
    private String descricao;

    @Column(nullable = false, length = 10)
    private String sigla;

    public UnidadeMedida() {}

    public Integer getIdUnidadeMedida() {
        return idUnidadeMedida;
    }

    public void setIdUnidadeMedida(Integer idUnidadeMedida) {
        this.idUnidadeMedida = idUnidadeMedida;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getSigla() {
        return sigla;
    }

    public void setSigla(String sigla) {
        this.sigla = sigla;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UnidadeMedida that = (UnidadeMedida) o;
        return Objects.equals(idUnidadeMedida, that.idUnidadeMedida);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idUnidadeMedida);
    }
}
