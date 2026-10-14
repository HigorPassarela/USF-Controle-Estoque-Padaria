package br.com.controleestoque.service;

import br.com.controleestoque.api.request.UnidadeMedidaRequest;
import br.com.controleestoque.api.response.UnidadeMedidaResponse;
import br.com.controleestoque.exception.RecursoNaoEncontradoException;
import br.com.controleestoque.exception.RegraNegocioException;
import br.com.controleestoque.model.UnidadeMedida;
import br.com.controleestoque.repository.UnidadeMedidaRepository;
import br.com.controleestoque.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UnidadeMedidaService {

    private final UnidadeMedidaRepository repository;
    private final ProdutoRepository produtoRepository;

    @Transactional(readOnly = true)
    public List<UnidadeMedidaResponse> listarTodos() {
        return repository.findAll().stream().map(UnidadeMedidaResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public UnidadeMedidaResponse buscar(Integer id) {
        return UnidadeMedidaResponse.fromEntity(buscarEntidade(id));
    }

    @Transactional
    public UnidadeMedidaResponse criar(UnidadeMedidaRequest request) {
        UnidadeMedida e = new UnidadeMedida();
        e.setDescricao(request.descricao());
        e.setSigla(request.sigla());
        return UnidadeMedidaResponse.fromEntity(repository.save(e));
    }

    @Transactional
    public UnidadeMedidaResponse atualizar(Integer id, UnidadeMedidaRequest request) {
        UnidadeMedida e = buscarEntidade(id);
        e.setDescricao(request.descricao());
        e.setSigla(request.sigla());
        return UnidadeMedidaResponse.fromEntity(repository.save(e));
    }

    @Transactional
    public void excluir(Integer id) {
        UnidadeMedida e = buscarEntidade(id);
        if (produtoRepository.existsByUnidadeMedida_IdUnidadeMedida(id)) {
            throw new RegraNegocioException("Unidade de medida está em uso por produtos e não pode ser excluída");
        }
        repository.delete(e);
        repository.flush();
    }

    private UnidadeMedida buscarEntidade(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Unidade de medida não encontrada"));
    }
}
