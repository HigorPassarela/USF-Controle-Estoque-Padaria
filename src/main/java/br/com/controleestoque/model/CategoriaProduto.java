package br.com.controleestoque.model;
import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "categoria_produto")
public class CategoriaProduto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria_produto")
    private Integer idCategoriaProduto;

    @Column(nullable = false, length = 100)
    private String descricao;

    public CategoriaProduto() {}

    public Integer getIdCategoriaProduto() {
        return idCategoriaProduto;
    }

    public void setIdCategoriaProduto(Integer idCategoriaProduto) {
        this.idCategoriaProduto = idCategoriaProduto;
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
        CategoriaProduto that = (CategoriaProduto) o;
        return Objects.equals(idCategoriaProduto, that.idCategoriaProduto);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idCategoriaProduto);
    }
}
