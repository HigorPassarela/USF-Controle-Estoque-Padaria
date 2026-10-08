package br.com.controleestoque.service;

import br.com.controleestoque.api.request.ClienteRequest;
import br.com.controleestoque.api.response.ClienteResponse;
import br.com.controleestoque.model.Cliente;
import br.com.controleestoque.repository.CidadeRepository;
import br.com.controleestoque.repository.ClienteRepository;
import br.com.controleestoque.repository.TipoClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private TipoClienteRepository tipoClienteRepository;

    @Autowired
    private CidadeRepository cidadeRepository;

    @Transactional(readOnly = true)
    public List<ClienteResponse> listarTodos() {
        return clienteRepository.findAll()
                .stream()
                .map(ClienteResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public ClienteResponse salvar(ClienteRequest request) {
        Cliente cliente = new Cliente();
        cliente.setNome(request.nome());
        cliente.setCpfCnpj(request.cpfCnpj());
        cliente.setTelefone(request.telefone());
        cliente.setEmail(request.email());

        cliente.setTipoCliente(tipoClienteRepository.findById(request.idTipoCliente())
                .orElseThrow(() -> new RuntimeException("Tipo de Cliente não encontrado")));

        if (request.idCidade() != null) {
            cliente.setCidade(cidadeRepository.findById(request.idCidade())
                    .orElseThrow(() -> new RuntimeException("Cidade não encontrada")));
        }

        cliente = clienteRepository.save(cliente);
        return ClienteResponse.fromEntity(cliente);
    }
}