package br.com.controleestoque.repository;

import br.com.controleestoque.model.MovimentacaoEstoque;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, Integer>,
        JpaSpecificationExecutor<MovimentacaoEstoque> {

    @Override
    @EntityGraph(attributePaths = {"lote", "produto", "produto.unidadeMedida", "tipoMovimentacao"})
    List<MovimentacaoEstoque> findAll(Specification<MovimentacaoEstoque> spec, Sort sort);

    @Override
    @EntityGraph(attributePaths = {"lote", "produto", "tipoMovimentacao"})
    Optional<MovimentacaoEstoque> findById(Integer idMovimentacao);

    boolean existsByTipoMovimentacao_IdTipoMovimentacao(Integer idTipoMovimentacao);
}
