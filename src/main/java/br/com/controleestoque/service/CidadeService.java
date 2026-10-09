package br.com.controleestoque.service;

import br.com.controleestoque.api.request.CidadeRequest;
import br.com.controleestoque.api.response.CidadeResponse;
import br.com.controleestoque.exception.RecursoNaoEncontradoException;
import br.com.controleestoque.exception.RegraNegocioException;
import br.com.controleestoque.model.Cidade;
import br.com.controleestoque.repository.CidadeRepository;
import br.com.controleestoque.repository.ClienteRepository;
import br.com.controleestoque.repository.EstadoRepository;
import br.com.controleestoque.repository.FornecedorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CidadeService {

    private final CidadeRepository repository;
    private final EstadoRepository estadoRepository;
    private final ClienteRepository clienteRepository;
    private final FornecedorRepository fornecedorRepository;

    @Transactional(readOnly = true)
    public List<CidadeResponse> listarTodos() {
        return repository.findAll().stream().map(CidadeResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public CidadeResponse buscar(Integer id) {
        return CidadeResponse.fromEntity(buscarEntidade(id));
    }

    @Transactional
    public CidadeResponse criar(CidadeRequest request) {
        Cidade cidade = new Cidade();
        preencher(cidade, request);
        return CidadeResponse.fromEntity(repository.save(cidade));
    }

    @Transactional
    public CidadeResponse atualizar(Integer id, CidadeRequest request) {
        Cidade cidade = buscarEntidade(id);
        preencher(cidade, request);
        return CidadeResponse.fromEntity(repository.save(cidade));
    }

    @Transactional
    public void excluir(Integer id) {
        Cidade cidade = buscarEntidade(id);
        if (clienteRepository.existsByCidade_IdCidade(id) || fornecedorRepository.existsByCidade_IdCidade(id)) {
            throw new RegraNegocioException("Cidade está em uso por clientes ou fornecedores e não pode ser excluída");
        }
        repository.delete(cidade);
        repository.flush();
    }

    private Cidade buscarEntidade(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cidade não encontrada"));
    }

    private void preencher(Cidade cidade, CidadeRequest request) {
        cidade.setNome(request.nome());
        cidade.setEstado(estadoRepository.findById(OpcaoSelecionada.id(request.estado(), "estado"))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Estado não encontrado")));
    }
}
