package br.com.controleestoque.repository;

import br.com.controleestoque.model.Bairro;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BairroRepository extends JpaRepository<Bairro, Integer> {

    @Override
    @EntityGraph(attributePaths = {"cidade", "cidade.estado"})
    List<Bairro> findAll();

    @Override
    @EntityGraph(attributePaths = {"cidade", "cidade.estado"})
    Optional<Bairro> findById(Integer idBairro);

    boolean existsByCidade_IdCidade(Integer idCidade);
}
