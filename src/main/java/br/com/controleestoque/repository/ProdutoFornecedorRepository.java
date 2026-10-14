package br.com.controleestoque.repository;

import br.com.controleestoque.model.ProdutoFornecedor;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProdutoFornecedorRepository extends JpaRepository<ProdutoFornecedor, Integer> {

    @EntityGraph(attributePaths = {"fornecedor"})
    List<ProdutoFornecedor> findByProduto_IdProdutoAndFornecedor_AtivoTrueOrderByFornecedor_NomeAsc(Integer idProduto);

    @EntityGraph(attributePaths = {"fornecedor"})
    Optional<ProdutoFornecedor> findByProduto_IdProdutoAndFornecedor_IdFornecedor(Integer idProduto, Integer idFornecedor);

    boolean existsByProduto_IdProdutoAndFornecedor_IdFornecedor(Integer idProduto, Integer idFornecedor);
}
