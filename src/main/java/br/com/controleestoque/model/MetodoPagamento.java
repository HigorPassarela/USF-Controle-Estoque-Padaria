package br.com.controleestoque.model;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "metodo_pagamento")
public class MetodoPagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_metodo_pagamento")
    private Integer idMetodoPagamento;

    @Column(nullable = false, length = 100)
    private String descricao;

    public MetodoPagamento() {}

    public Integer getIdMetodoPagamento() {
        return idMetodoPagamento;
    }

    public void setIdMetodoPagamento(Integer idMetodoPagamento) {
        this.idMetodoPagamento = idMetodoPagamento;
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
        MetodoPagamento that = (MetodoPagamento) o;
        return Objects.equals(idMetodoPagamento, that.idMetodoPagamento);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idMetodoPagamento);
    }
}
