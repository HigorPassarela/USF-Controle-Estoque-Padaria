package br.com.controleestoque.service;

import br.com.controleestoque.api.request.CategoriaProdutoRequest;
import br.com.controleestoque.api.response.CategoriaProdutoResponse;
import br.com.controleestoque.exception.RecursoNaoEncontradoException;
import br.com.controleestoque.exception.RegraNegocioException;
import br.com.controleestoque.model.CategoriaProduto;
import br.com.controleestoque.repository.CategoriaProdutoRepository;
import br.com.controleestoque.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaProdutoService {

    private final CategoriaProdutoRepository repository;
    private final ProdutoRepository produtoRepository;

    @Transactional(readOnly = true)
    public List<CategoriaProdutoResponse> listarTodos() {
        return repository.findAll().stream().map(CategoriaProdutoResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public CategoriaProdutoResponse buscar(Integer id) {
        return CategoriaProdutoResponse.fromEntity(buscarEntidade(id));
    }

    @Transactional
    public CategoriaProdutoResponse criar(CategoriaProdutoRequest request) {
        CategoriaProduto e = new CategoriaProduto();
        e.setDescricao(request.descricao());
        return CategoriaProdutoResponse.fromEntity(repository.save(e));
    }

    @Transactional
    public CategoriaProdutoResponse atualizar(Integer id, CategoriaProdutoRequest request) {
        CategoriaProduto e = buscarEntidade(id);
        e.setDescricao(request.descricao());
        return CategoriaProdutoResponse.fromEntity(repository.save(e));
    }

    @Transactional
    public void excluir(Integer id) {
        CategoriaProduto e = buscarEntidade(id);
        if (produtoRepository.existsByCategoriaProduto_IdCategoriaProduto(id)) {
            throw new RegraNegocioException("Categoria de produto está em uso por produtos e não pode ser excluída");
        }
        repository.delete(e);
        repository.flush();
    }

    private CategoriaProduto buscarEntidade(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria de produto não encontrada"));
    }
}
