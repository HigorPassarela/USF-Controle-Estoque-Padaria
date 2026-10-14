package br.com.controleestoque.repository;

import br.com.controleestoque.model.Produto;
import org.springframework.data.jpa.repository.EntityGraph;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Integer> {

    @EntityGraph(attributePaths = {"categoriaProduto", "tipoProduto", "unidadeMedida", "fornecedor"})
    List<Produto> findAllByAtivoTrue();

    @EntityGraph(attributePaths = {"categoriaProduto", "tipoProduto", "unidadeMedida", "fornecedor"})
    Optional<Produto> findByIdProdutoAndAtivoTrue(Integer idProduto);

    /**
     * Bloqueia a linha do produto até o fim da transação (SELECT ... FOR UPDATE). Toda operação de
     * estoque começa por aqui, o que serializa entradas e saídas do mesmo produto.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Produto p join fetch p.unidadeMedida where p.idProduto = :idProduto")
    Optional<Produto> findParaMovimentarEstoque(@Param("idProduto") Integer idProduto);

    boolean existsByCategoriaProduto_IdCategoriaProduto(Integer idCategoriaProduto);

    boolean existsByTipoProduto_IdTipoProduto(Integer idTipoProduto);

    boolean existsByUnidadeMedida_IdUnidadeMedida(Integer idUnidadeMedida);
}
