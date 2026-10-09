package br.com.controleestoque.service;

import br.com.controleestoque.api.request.FornecedorRequest;
import br.com.controleestoque.api.response.FornecedorResponse;
import br.com.controleestoque.exception.RecursoNaoEncontradoException;
import br.com.controleestoque.exception.RegraNegocioException;
import br.com.controleestoque.model.Fornecedor;
import br.com.controleestoque.repository.CidadeRepository;
import br.com.controleestoque.repository.FornecedorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FornecedorService {

    private final FornecedorRepository fornecedorRepository;
    private final CidadeRepository cidadeRepository;

    @Transactional(readOnly = true)
    public List<FornecedorResponse> listarTodos() {
        return fornecedorRepository.findAllByAtivoTrue()
                .stream()
                .map(FornecedorResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public FornecedorResponse buscar(Integer id) {
        return FornecedorResponse.fromEntity(buscarAtivo(id));
    }

    @Transactional
    public FornecedorResponse criar(FornecedorRequest request) {
        if (fornecedorRepository.existsByCnpjAndAtivoTrue(request.cnpj())) {
            throw new RegraNegocioException("Já existe um fornecedor ativo com este CNPJ");
        }
        Fornecedor fornecedor = new Fornecedor();
        preencher(fornecedor, request);
        return FornecedorResponse.fromEntity(fornecedorRepository.save(fornecedor));
    }

    @Transactional
    public FornecedorResponse atualizar(Integer id, FornecedorRequest request) {
        Fornecedor fornecedor = buscarAtivo(id);
        if (fornecedorRepository.existsByCnpjAndAtivoTrueAndIdFornecedorNot(request.cnpj(), id)) {
            throw new RegraNegocioException("Já existe um fornecedor ativo com este CNPJ");
        }
        preencher(fornecedor, request);
        return FornecedorResponse.fromEntity(fornecedorRepository.save(fornecedor));
    }

    @Transactional
    public void excluir(Integer id) {
        Fornecedor fornecedor = buscarAtivo(id);
        fornecedor.setAtivo(false);
        fornecedorRepository.save(fornecedor);
    }

    private Fornecedor buscarAtivo(Integer id) {
        return fornecedorRepository.findByIdFornecedorAndAtivoTrue(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Fornecedor não encontrado"));
    }

    private void preencher(Fornecedor fornecedor, FornecedorRequest request) {
        fornecedor.setNome(request.nome());
        fornecedor.setCnpj(request.cnpj());
        fornecedor.setTelefone(request.telefone());
        fornecedor.setEmail(request.email());

        Integer idCidade = OpcaoSelecionada.idOuNulo(request.cidade(), "cidade");
        fornecedor.setCidade(idCidade == null ? null
                : cidadeRepository.findById(idCidade)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cidade não encontrada")));
    }
}
