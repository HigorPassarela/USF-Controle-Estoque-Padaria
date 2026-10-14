package br.com.controleestoque.repository;

import br.com.controleestoque.model.Lote;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoteRepository extends JpaRepository<Lote, Integer>, JpaSpecificationExecutor<Lote> {

    /**
     * Lotes do produto que podem sair numa baixa FEFO: com saldo e não vencidos, na ordem em que
     * devem ser consumidos (validade mais próxima primeiro, sem validade por último, depois o mais antigo).
     */
    @Query("""
            select l from Lote l
            where l.produto.idProduto = :idProduto
              and l.quantidadeAtual > 0
              and (l.dataValidade is null or l.dataValidade >= :hoje)
            order by l.dataValidade asc nulls last, l.dataEntrada asc, l.idLote asc
            """)
    List<Lote> findDisponiveisFefo(@Param("idProduto") Integer idProduto, @Param("hoje") LocalDate hoje);

    /** Id do produto do lote, sem carregar o lote (usado antes de bloquear o produto). */
    @Query("select l.produto.idProduto from Lote l where l.idLote = :idLote")
    Optional<Integer> findIdProdutoDoLote(@Param("idLote") Integer idLote);

    @Override
    @EntityGraph(attributePaths = {"produto", "produto.unidadeMedida", "fornecedor"})
    Optional<Lote> findById(Integer idLote);

    @Override
    @EntityGraph(attributePaths = {"produto", "produto.unidadeMedida", "fornecedor"})
    List<Lote> findAll(Specification<Lote> spec, Sort sort);

    @EntityGraph(attributePaths = {"produto", "produto.unidadeMedida"})
    List<Lote> findByQuantidadeAtualGreaterThan(BigDecimal quantidade);

    List<Lote> findByQuantidadeAtualGreaterThanAndProduto_AtivoTrue(BigDecimal quantidade);

    List<Lote> findByProduto_IdProdutoAndQuantidadeAtualGreaterThan(Integer idProduto, BigDecimal quantidade);
}
