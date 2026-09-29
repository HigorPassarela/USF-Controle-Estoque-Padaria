package br.com.controleestoque.model;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "tipo_cliente")
public class TipoCliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_cliente")
    private Integer idTipoCliente;

    @Column(nullable = false, length = 100)
    private String descricao;

    public TipoCliente() {}

    public Integer getIdTipoCliente() {
        return idTipoCliente;
    }

    public void setIdTipoCliente(Integer idTipoCliente) {
        this.idTipoCliente = idTipoCliente;
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
        TipoCliente that = (TipoCliente) o;
        return Objects.equals(idTipoCliente, that.idTipoCliente);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idTipoCliente);
    }
}
