package br.com.controleestoque.service;

import br.com.controleestoque.api.request.TipoClienteRequest;
import br.com.controleestoque.api.response.TipoClienteResponse;
import br.com.controleestoque.exception.RecursoNaoEncontradoException;
import br.com.controleestoque.exception.RegraNegocioException;
import br.com.controleestoque.model.TipoCliente;
import br.com.controleestoque.repository.TipoClienteRepository;
import br.com.controleestoque.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TipoClienteService {

    private final TipoClienteRepository repository;
    private final ClienteRepository clienteRepository;

    @Transactional(readOnly = true)
    public List<TipoClienteResponse> listarTodos() {
        return repository.findAll().stream().map(TipoClienteResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public TipoClienteResponse buscar(Integer id) {
        return TipoClienteResponse.fromEntity(buscarEntidade(id));
    }

    @Transactional
    public TipoClienteResponse criar(TipoClienteRequest request) {
        TipoCliente e = new TipoCliente();
        e.setDescricao(request.descricao());
        return TipoClienteResponse.fromEntity(repository.save(e));
    }

    @Transactional
    public TipoClienteResponse atualizar(Integer id, TipoClienteRequest request) {
        TipoCliente e = buscarEntidade(id);
        e.setDescricao(request.descricao());
        return TipoClienteResponse.fromEntity(repository.save(e));
    }

    @Transactional
    public void excluir(Integer id) {
        TipoCliente e = buscarEntidade(id);
        if (clienteRepository.existsByTipoCliente_IdTipoCliente(id)) {
            throw new RegraNegocioException("Tipo de cliente está em uso por clientes e não pode ser excluído");
        }
        repository.delete(e);
        repository.flush();
    }

    private TipoCliente buscarEntidade(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Tipo de cliente não encontrado"));
    }
}
