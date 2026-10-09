package br.com.controleestoque.service;

import br.com.controleestoque.api.request.ClienteRequest;
import br.com.controleestoque.api.response.ClienteResponse;
import br.com.controleestoque.exception.RecursoNaoEncontradoException;
import br.com.controleestoque.exception.RegraNegocioException;
import br.com.controleestoque.model.Cliente;
import br.com.controleestoque.repository.CidadeRepository;
import br.com.controleestoque.repository.ClienteRepository;
import br.com.controleestoque.repository.TipoClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final TipoClienteRepository tipoClienteRepository;
    private final CidadeRepository cidadeRepository;

    @Transactional(readOnly = true)
    public List<ClienteResponse> listarTodos() {
        return clienteRepository.findAllByAtivoTrue()
                .stream()
                .map(ClienteResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public ClienteResponse buscar(Integer id) {
        return ClienteResponse.fromEntity(buscarAtivo(id));
    }

    @Transactional
    public ClienteResponse criar(ClienteRequest request) {
        if (clienteRepository.existsByCpfCnpjAndAtivoTrue(request.cpfCnpj())) {
            throw new RegraNegocioException("Já existe um cliente ativo com este CPF/CNPJ");
        }
        Cliente cliente = new Cliente();
        preencher(cliente, request);
        return ClienteResponse.fromEntity(clienteRepository.save(cliente));
    }

    @Transactional
    public ClienteResponse atualizar(Integer id, ClienteRequest request) {
        Cliente cliente = buscarAtivo(id);
        if (clienteRepository.existsByCpfCnpjAndAtivoTrueAndIdClienteNot(request.cpfCnpj(), id)) {
            throw new RegraNegocioException("Já existe um cliente ativo com este CPF/CNPJ");
        }
        preencher(cliente, request);
        return ClienteResponse.fromEntity(clienteRepository.save(cliente));
    }

    @Transactional
    public void excluir(Integer id) {
        Cliente cliente = buscarAtivo(id);
        cliente.setAtivo(false);
        clienteRepository.save(cliente);
    }

    private Cliente buscarAtivo(Integer id) {
        return clienteRepository.findByIdClienteAndAtivoTrue(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado"));
    }

    private void preencher(Cliente cliente, ClienteRequest request) {
        cliente.setNome(request.nome());
        cliente.setCpfCnpj(request.cpfCnpj());
        cliente.setTelefone(request.telefone());
        cliente.setEmail(request.email());

        cliente.setTipoCliente(tipoClienteRepository.findById(OpcaoSelecionada.id(request.tipoCliente(), "tipoCliente"))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Tipo de Cliente não encontrado")));

        Integer idCidade = OpcaoSelecionada.idOuNulo(request.cidade(), "cidade");
        cliente.setCidade(idCidade == null ? null
                : cidadeRepository.findById(idCidade)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cidade não encontrada")));
    }
}
