package br.com.controleestoque.service;

import br.com.controleestoque.api.request.MetodoPagamentoRequest;
import br.com.controleestoque.api.response.MetodoPagamentoResponse;
import br.com.controleestoque.exception.RecursoNaoEncontradoException;
import br.com.controleestoque.model.MetodoPagamento;
import br.com.controleestoque.repository.MetodoPagamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MetodoPagamentoService {

    private final MetodoPagamentoRepository repository;

    @Transactional(readOnly = true)
    public List<MetodoPagamentoResponse> listarTodos() {
        return repository.findAll()
                .stream()
                .map(MetodoPagamentoResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public MetodoPagamentoResponse buscar(Integer id) {
        return MetodoPagamentoResponse.fromEntity(buscarEntidade(id));
    }

    @Transactional
    public MetodoPagamentoResponse criar(MetodoPagamentoRequest request) {
        MetodoPagamento metodo = new MetodoPagamento();
        metodo.setDescricao(request.descricao());
        return MetodoPagamentoResponse.fromEntity(repository.save(metodo));
    }

    @Transactional
    public MetodoPagamentoResponse atualizar(Integer id, MetodoPagamentoRequest request) {
        MetodoPagamento metodo = buscarEntidade(id);
        metodo.setDescricao(request.descricao());
        return MetodoPagamentoResponse.fromEntity(repository.save(metodo));
    }

    @Transactional
    public void excluir(Integer id) {
        // Ainda não há tabela que referencie métodos de pagamento; quando houver (pagamento),
        // a verificação de "em uso" entra aqui. Até lá, o handler converte violação de FK em 409.
        MetodoPagamento metodo = buscarEntidade(id);
        repository.delete(metodo);
        repository.flush();
    }

    private MetodoPagamento buscarEntidade(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Método de pagamento não encontrado"));
    }
}
