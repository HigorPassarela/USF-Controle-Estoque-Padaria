package br.com.controleestoque.service;

import br.com.controleestoque.api.request.FornecedorRequest;
import br.com.controleestoque.api.response.FornecedorResponse;
import br.com.controleestoque.model.Fornecedor;
import br.com.controleestoque.repository.CidadeRepository;
import br.com.controleestoque.repository.FornecedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FornecedorService {

    @Autowired
    private FornecedorRepository fornecedorRepository;

    @Autowired
    private CidadeRepository cidadeRepository;

    @Transactional(readOnly = true)
    public List<FornecedorResponse> listarTodos() {
        return fornecedorRepository.findAll()
                .stream()
                .map(FornecedorResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public FornecedorResponse salvar(FornecedorRequest request) {
        Fornecedor fornecedor = new Fornecedor();
        fornecedor.setNome(request.nome());
        fornecedor.setCnpj(request.cnpj());
        fornecedor.setTelefone(request.telefone());
        fornecedor.setEmail(request.email());

        if (request.idCidade() != null) {
            fornecedor.setCidade(cidadeRepository.findById(request.idCidade())
                    .orElseThrow(() -> new RuntimeException("Cidade não encontrada")));
        }

        fornecedor = fornecedorRepository.save(fornecedor);
        return FornecedorResponse.fromEntity(fornecedor);
    }
}
