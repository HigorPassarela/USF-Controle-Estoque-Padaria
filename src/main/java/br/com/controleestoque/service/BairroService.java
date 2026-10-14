package br.com.controleestoque.service;

import br.com.controleestoque.api.request.BairroRequest;
import br.com.controleestoque.api.response.BairroResponse;
import br.com.controleestoque.exception.RecursoNaoEncontradoException;
import br.com.controleestoque.exception.RegraNegocioException;
import br.com.controleestoque.exception.ValorInvalidoException;
import br.com.controleestoque.model.Bairro;
import br.com.controleestoque.model.Cidade;
import br.com.controleestoque.repository.BairroRepository;
import br.com.controleestoque.repository.CidadeRepository;
import br.com.controleestoque.repository.ClienteRepository;
import br.com.controleestoque.repository.FornecedorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BairroService {

    private final BairroRepository repository;
    private final CidadeRepository cidadeRepository;
    private final ClienteRepository clienteRepository;
    private final FornecedorRepository fornecedorRepository;

    @Transactional(readOnly = true)
    public List<BairroResponse> listarTodos() {
        return repository.findAll().stream().map(BairroResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public BairroResponse buscar(Integer id) {
        return BairroResponse.fromEntity(buscarEntidade(id));
    }

    @Transactional
    public BairroResponse criar(BairroRequest request) {
        Bairro bairro = new Bairro();
        preencher(bairro, request);
        return BairroResponse.fromEntity(repository.save(bairro));
    }

    @Transactional
    public BairroResponse atualizar(Integer id, BairroRequest request) {
        Bairro bairro = buscarEntidade(id);
        preencher(bairro, request);
        return BairroResponse.fromEntity(repository.save(bairro));
    }

    @Transactional
    public void excluir(Integer id) {
        Bairro bairro = buscarEntidade(id);
        if (clienteRepository.existsByBairro_IdBairro(id) || fornecedorRepository.existsByBairro_IdBairro(id)) {
            throw new RegraNegocioException("Bairro está em uso por clientes ou fornecedores e não pode ser excluído");
        }
        repository.delete(bairro);
        repository.flush();
    }

    /**
     * Resolve o bairro opcional do endereço de cliente ou fornecedor: devolve null se não informado,
     * e exige que exista e pertença à cidade escolhida no mesmo cadastro.
     */
    @Transactional(readOnly = true)
    public Bairro bairroDoEndereco(String valor, Cidade cidade) {
        Integer idBairro = OpcaoSelecionada.idOuNulo(valor, "bairro");
        if (idBairro == null) {
            return null;
        }
        if (cidade == null) {
            throw new ValorInvalidoException("cidade", "A cidade é obrigatória quando o bairro é informado");
        }
        Bairro bairro = buscarEntidade(idBairro);
        if (!bairro.getCidade().getIdCidade().equals(cidade.getIdCidade())) {
            throw new ValorInvalidoException("bairro", "O bairro não pertence à cidade informada");
        }
        return bairro;
    }

    private Bairro buscarEntidade(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Bairro não encontrado"));
    }

    private void preencher(Bairro bairro, BairroRequest request) {
        bairro.setNome(request.nome());
        bairro.setCidade(cidadeRepository.findById(OpcaoSelecionada.id(request.cidade(), "cidade"))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cidade não encontrada")));
    }
}
