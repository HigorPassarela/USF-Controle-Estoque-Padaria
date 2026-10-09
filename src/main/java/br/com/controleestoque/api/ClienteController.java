package br.com.controleestoque.api;

import br.com.controleestoque.api.handler.ErroResponse;
import br.com.controleestoque.api.request.ClienteRequest;
import br.com.controleestoque.api.response.ClienteResponse;
import br.com.controleestoque.service.OpcaoSelecionada;
import br.com.controleestoque.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
@Tag(name = "Clientes", description = "Cadastro de clientes da padaria. A exclusão é lógica: o cliente fica inativo e some das consultas.")
public class ClienteController {

    private final ClienteService service;

    @GetMapping
    @Operation(summary = "Listar clientes",
            description = "Retorna a lista de clientes cadastrados. Retorna apenas os registros ativos; os excluídos não aparecem.")
    @ApiResponse(responseCode = "200", description = "Lista retornada (vazia se não houver registros)")
    public List<ClienteResponse> listar() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar cliente por id",
            description = "Retorna os dados de um cliente a partir do seu id.")
    @ApiResponse(responseCode = "200", description = "Cliente encontrado")
    @ApiResponse(responseCode = "404", description = "O cliente não existe ou está inativo",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    public ClienteResponse buscar(@Parameter(description = "Escolha na lista (cliente)") @PathVariable String id) {
        return service.buscar(OpcaoSelecionada.id(id, "id"));
    }

    @PostMapping
    @Operation(summary = "Cadastrar cliente",
            description = "Cria um novo cliente: preencha os campos abaixo (os marcados com * são obrigatórios) e clique em Execute. Devolve o registro criado com o seu id. O registro nasce ativo.")
    @ApiResponse(responseCode = "201", description = "Cliente criado; o cabeçalho Location aponta para o novo recurso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos: nome, CPF/CNPJ e tipo de cliente são obrigatórios; o e-mail, se informado, deve ter formato válido",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "404", description = "Referência inexistente: o tipo de cliente ou a cidade informados não existem",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "409", description = "Conflito: já existe um cliente ativo com o mesmo CPF/CNPJ",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    public ResponseEntity<ClienteResponse> criar(@Valid @ParameterObject ClienteRequest request) {
        ClienteResponse criado = service.criar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{id}").buildAndExpand(criado.idCliente()).toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar cliente",
            description = "Altera o cliente: informe o id e preencha os campos abaixo (os marcados com * são obrigatórios). Os dados atuais são substituídos pelos informados; campos opcionais deixados em branco são apagados.")
    @ApiResponse(responseCode = "200", description = "Cliente atualizado")
    @ApiResponse(responseCode = "400", description = "Dados inválidos: nome, CPF/CNPJ e tipo de cliente são obrigatórios; o e-mail, se informado, deve ter formato válido",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "404", description = "O cliente não existe ou está inativo, ou referência inexistente (o tipo de cliente ou a cidade informados não existem)",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "409", description = "Conflito: já existe um cliente ativo com o mesmo CPF/CNPJ (em outro registro)",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    public ClienteResponse atualizar(@Parameter(description = "Escolha na lista (cliente)") @PathVariable String id,
                                  @Valid @ParameterObject ClienteRequest request) {
        return service.atualizar(OpcaoSelecionada.id(id, "id"), request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir cliente",
            description = "Exclusão lógica: marca o cliente como inativo. Ele deixa de aparecer nas listagens e nas buscas (404), mas o registro e o histórico são preservados.")
    @ApiResponse(responseCode = "204", description = "Cliente excluído (inativado)")
    @ApiResponse(responseCode = "404", description = "O cliente não existe ou já está inativo",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    public void excluir(@Parameter(description = "Escolha na lista (cliente)") @PathVariable String id) {
        service.excluir(OpcaoSelecionada.id(id, "id"));
    }
}
