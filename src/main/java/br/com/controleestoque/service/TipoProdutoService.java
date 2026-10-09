package br.com.controleestoque.service;

import br.com.controleestoque.api.request.TipoProdutoRequest;
import br.com.controleestoque.api.response.TipoProdutoResponse;
import br.com.controleestoque.exception.RecursoNaoEncontradoException;
import br.com.controleestoque.exception.RegraNegocioException;
import br.com.controleestoque.model.TipoProduto;
import br.com.controleestoque.repository.TipoProdutoRepository;
import br.com.controleestoque.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TipoProdutoService {

    private final TipoProdutoRepository repository;
    private final ProdutoRepository produtoRepository;

    @Transactional(readOnly = true)
    public List<TipoProdutoResponse> listarTodos() {
        return repository.findAll().stream().map(TipoProdutoResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public TipoProdutoResponse buscar(Integer id) {
        return TipoProdutoResponse.fromEntity(buscarEntidade(id));
    }

    @Transactional
    public TipoProdutoResponse criar(TipoProdutoRequest request) {
        TipoProduto e = new TipoProduto();
        e.setDescricao(request.descricao());
        return TipoProdutoResponse.fromEntity(repository.save(e));
    }

    @Transactional
    public TipoProdutoResponse atualizar(Integer id, TipoProdutoRequest request) {
        TipoProduto e = buscarEntidade(id);
        e.setDescricao(request.descricao());
        return TipoProdutoResponse.fromEntity(repository.save(e));
    }

    @Transactional
    public void excluir(Integer id) {
        TipoProduto e = buscarEntidade(id);
        if (produtoRepository.existsByTipoProduto_IdTipoProduto(id)) {
            throw new RegraNegocioException("Tipo de produto está em uso por produtos e não pode ser excluído");
        }
        repository.delete(e);
        repository.flush();
    }

    private TipoProduto buscarEntidade(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Tipo de produto não encontrado"));
    }
}
