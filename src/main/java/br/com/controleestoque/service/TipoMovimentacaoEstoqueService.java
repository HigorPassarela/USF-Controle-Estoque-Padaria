package br.com.controleestoque.service;

import br.com.controleestoque.api.request.TipoMovimentacaoEstoqueRequest;
import br.com.controleestoque.api.response.TipoMovimentacaoEstoqueResponse;
import br.com.controleestoque.exception.RecursoNaoEncontradoException;
import br.com.controleestoque.exception.RegraNegocioException;
import br.com.controleestoque.model.TipoMovimentacaoEstoque;
import br.com.controleestoque.repository.MovimentacaoEstoqueRepository;
import br.com.controleestoque.repository.TipoMovimentacaoEstoqueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TipoMovimentacaoEstoqueService {

    private final TipoMovimentacaoEstoqueRepository repository;
    private final MovimentacaoEstoqueRepository movimentacaoRepository;

    @Transactional(readOnly = true)
    public List<TipoMovimentacaoEstoqueResponse> listarTodos() {
        return repository.findAll().stream().map(TipoMovimentacaoEstoqueResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public TipoMovimentacaoEstoqueResponse buscar(Integer id) {
        return TipoMovimentacaoEstoqueResponse.fromEntity(buscarEntidade(id));
    }

    @Transactional
    public TipoMovimentacaoEstoqueResponse criar(TipoMovimentacaoEstoqueRequest request) {
        TipoMovimentacaoEstoque e = new TipoMovimentacaoEstoque();
        e.setDescricao(request.descricao());
        e.setTipo(request.tipo());
        return TipoMovimentacaoEstoqueResponse.fromEntity(repository.save(e));
    }

    @Transactional
    public TipoMovimentacaoEstoqueResponse atualizar(Integer id, TipoMovimentacaoEstoqueRequest request) {
        TipoMovimentacaoEstoque e = buscarEntidade(id);
        // Trocar a natureza de um tipo já usado reescreveria o sentido das movimentações antigas.
        if (!e.getTipo().equals(request.tipo()) && movimentacaoRepository.existsByTipoMovimentacao_IdTipoMovimentacao(id)) {
            throw new RegraNegocioException("Tipo de movimentação já usado em movimentações não pode trocar de natureza (ENTRADA/SAIDA)");
        }
        e.setDescricao(request.descricao());
        e.setTipo(request.tipo());
        return TipoMovimentacaoEstoqueResponse.fromEntity(repository.save(e));
    }

    @Transactional
    public void excluir(Integer id) {
        TipoMovimentacaoEstoque e = buscarEntidade(id);
        if (movimentacaoRepository.existsByTipoMovimentacao_IdTipoMovimentacao(id)) {
            throw new RegraNegocioException("Tipo de movimentação está em uso por movimentações de estoque e não pode ser excluído");
        }
        repository.delete(e);
        repository.flush();
    }

    private TipoMovimentacaoEstoque buscarEntidade(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Tipo de movimentação de estoque não encontrado"));
    }
}
