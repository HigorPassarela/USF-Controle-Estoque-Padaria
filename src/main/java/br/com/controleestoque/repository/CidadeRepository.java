package br.com.controleestoque.repository;

import br.com.controleestoque.model.Cidade;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CidadeRepository extends JpaRepository<Cidade, Integer> {

    @Override
    @EntityGraph(attributePaths = {"estado"})
    List<Cidade> findAll();
}
