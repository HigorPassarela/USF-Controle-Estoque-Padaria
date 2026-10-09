package br.com.controleestoque.repository;

import br.com.controleestoque.model.Produto;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Integer> {

    @EntityGraph(attributePaths = {"categoriaProduto", "tipoProduto", "unidadeMedida", "fornecedor"})
    List<Produto> findAllByAtivoTrue();

    @EntityGraph(attributePaths = {"categoriaProduto", "tipoProduto", "unidadeMedida", "fornecedor"})
    Optional<Produto> findByIdProdutoAndAtivoTrue(Integer idProduto);

    boolean existsByCategoriaProduto_IdCategoriaProduto(Integer idCategoriaProduto);

    boolean existsByTipoProduto_IdTipoProduto(Integer idTipoProduto);

    boolean existsByUnidadeMedida_IdUnidadeMedida(Integer idUnidadeMedida);
}
