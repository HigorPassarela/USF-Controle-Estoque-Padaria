package br.com.controleestoque.api;

import br.com.controleestoque.api.handler.ErroResponse;
import br.com.controleestoque.api.request.FornecedorRequest;
import br.com.controleestoque.api.response.FornecedorResponse;
import br.com.controleestoque.service.OpcaoSelecionada;
import br.com.controleestoque.service.FornecedorService;
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
@RequestMapping("/api/fornecedores")
@RequiredArgsConstructor
@Tag(name = "Fornecedores", description = "Cadastro de fornecedores. A exclusão é lógica: o fornecedor fica inativo e some das consultas.")
public class FornecedorController {

    private final FornecedorService service;

    @GetMapping
    @Operation(summary = "Listar fornecedores",
            description = "Retorna a lista de fornecedores cadastrados. Retorna apenas os registros ativos; os excluídos não aparecem.")
    @ApiResponse(responseCode = "200", description = "Lista retornada (vazia se não houver registros)")
    public List<FornecedorResponse> listar() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar fornecedor por id",
            description = "Retorna os dados de um fornecedor a partir do seu id.")
    @ApiResponse(responseCode = "200", description = "Fornecedor encontrado")
    @ApiResponse(responseCode = "404", description = "O fornecedor não existe ou está inativo",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    public FornecedorResponse buscar(@Parameter(description = "Escolha na lista (fornecedor)") @PathVariable String id) {
        return service.buscar(OpcaoSelecionada.id(id, "id"));
    }

    @PostMapping
    @Operation(summary = "Cadastrar fornecedor",
            description = "Cria um novo fornecedor: preencha os campos abaixo (os marcados com * são obrigatórios) e clique em Execute. Devolve o registro criado com o seu id. O registro nasce ativo.")
    @ApiResponse(responseCode = "201", description = "Fornecedor criado; o cabeçalho Location aponta para o novo recurso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos: nome e CNPJ são obrigatórios; o e-mail, se informado, deve ter formato válido",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "404", description = "Referência inexistente: a cidade informada não existe",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "409", description = "Conflito: já existe um fornecedor ativo com o mesmo CNPJ",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    public ResponseEntity<FornecedorResponse> criar(@Valid @ParameterObject FornecedorRequest request) {
        FornecedorResponse criado = service.criar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{id}").buildAndExpand(criado.idFornecedor()).toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar fornecedor",
            description = "Altera o fornecedor: informe o id e preencha os campos abaixo (os marcados com * são obrigatórios). Os dados atuais são substituídos pelos informados; campos opcionais deixados em branco são apagados.")
    @ApiResponse(responseCode = "200", description = "Fornecedor atualizado")
    @ApiResponse(responseCode = "400", description = "Dados inválidos: nome e CNPJ são obrigatórios; o e-mail, se informado, deve ter formato válido",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "404", description = "O fornecedor não existe ou está inativo, ou referência inexistente (a cidade informada não existe)",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "409", description = "Conflito: já existe um fornecedor ativo com o mesmo CNPJ (em outro registro)",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    public FornecedorResponse atualizar(@Parameter(description = "Escolha na lista (fornecedor)") @PathVariable String id,
                                  @Valid @ParameterObject FornecedorRequest request) {
        return service.atualizar(OpcaoSelecionada.id(id, "id"), request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir fornecedor",
            description = "Exclusão lógica: marca o fornecedor como inativo. Ele deixa de aparecer nas listagens e nas buscas (404), mas o registro e o histórico são preservados.")
    @ApiResponse(responseCode = "204", description = "Fornecedor excluído (inativado)")
    @ApiResponse(responseCode = "404", description = "O fornecedor não existe ou já está inativo",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    public void excluir(@Parameter(description = "Escolha na lista (fornecedor)") @PathVariable String id) {
        service.excluir(OpcaoSelecionada.id(id, "id"));
    }
}
