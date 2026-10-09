package br.com.controleestoque.repository;

import br.com.controleestoque.model.Fornecedor;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FornecedorRepository extends JpaRepository<Fornecedor, Integer> {

    @EntityGraph(attributePaths = {"cidade"})
    List<Fornecedor> findAllByAtivoTrue();

    @EntityGraph(attributePaths = {"cidade"})
    Optional<Fornecedor> findByIdFornecedorAndAtivoTrue(Integer idFornecedor);

    boolean existsByCnpjAndAtivoTrue(String cnpj);

    boolean existsByCnpjAndAtivoTrueAndIdFornecedorNot(String cnpj, Integer idFornecedor);

    boolean existsByCidade_IdCidade(Integer idCidade);
}
