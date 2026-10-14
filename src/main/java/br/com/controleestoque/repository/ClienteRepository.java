package br.com.controleestoque.repository;

import br.com.controleestoque.model.Cliente;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

    @EntityGraph(attributePaths = {"tipoCliente", "cidade", "bairro"})
    List<Cliente> findAllByAtivoTrue();

    @EntityGraph(attributePaths = {"tipoCliente", "cidade", "bairro"})
    Optional<Cliente> findByIdClienteAndAtivoTrue(Integer idCliente);

    boolean existsByCpfCnpjAndAtivoTrue(String cpfCnpj);

    boolean existsByCpfCnpjAndAtivoTrueAndIdClienteNot(String cpfCnpj, Integer idCliente);

    boolean existsByTipoCliente_IdTipoCliente(Integer idTipoCliente);

    boolean existsByCidade_IdCidade(Integer idCidade);

    boolean existsByBairro_IdBairro(Integer idBairro);
}
