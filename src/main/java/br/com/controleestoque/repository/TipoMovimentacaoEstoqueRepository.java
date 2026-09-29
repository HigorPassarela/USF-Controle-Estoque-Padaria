package br.com.controleestoque.repository;

import br.com.controleestoque.model.TipoMovimentacaoEstoque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TipoMovimentacaoEstoqueRepository extends JpaRepository<TipoMovimentacaoEstoque, Integer> {
}