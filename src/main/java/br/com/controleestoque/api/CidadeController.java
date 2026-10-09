package br.com.controleestoque.api;

import br.com.controleestoque.api.handler.ErroResponse;
import br.com.controleestoque.api.request.CidadeRequest;
import br.com.controleestoque.api.response.CidadeResponse;
import br.com.controleestoque.service.OpcaoSelecionada;
import br.com.controleestoque.service.CidadeService;
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
@RequestMapping("/api/cidades")
@RequiredArgsConstructor
@Tag(name = "Cidades", description = "Cidades usadas no endereço de clientes e fornecedores. Cada cidade pertence a um estado.")
public class CidadeController {

    private final CidadeService service;

    @GetMapping
    @Operation(summary = "Listar cidades",
            description = "Retorna a lista de cidades cadastrados.")
    @ApiResponse(responseCode = "200", description = "Lista retornada (vazia se não houver registros)")
    public List<CidadeResponse> listar() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar cidade por id",
            description = "Retorna os dados de um cidade a partir do seu id.")
    @ApiResponse(responseCode = "200", description = "Cidade encontrado")
    @ApiResponse(responseCode = "404", description = "O cidade não existe",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    public CidadeResponse buscar(@Parameter(description = "Escolha na lista (cidade)") @PathVariable String id) {
        return service.buscar(OpcaoSelecionada.id(id, "id"));
    }

    @PostMapping
    @Operation(summary = "Cadastrar cidade",
            description = "Cria um novo cidade: preencha os campos abaixo (os marcados com * são obrigatórios) e clique em Execute. Devolve o registro criado com o seu id.")
    @ApiResponse(responseCode = "201", description = "Cidade criado; o cabeçalho Location aponta para o novo recurso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos: o nome (até 100 caracteres) e o estado são obrigatórios",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "404", description = "Referência inexistente: o estado informado não existe",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    public ResponseEntity<CidadeResponse> criar(@Valid @ParameterObject CidadeRequest request) {
        CidadeResponse criado = service.criar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{id}").buildAndExpand(criado.idCidade()).toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar cidade",
            description = "Altera o cidade: informe o id e preencha os campos abaixo (os marcados com * são obrigatórios). Os dados atuais são substituídos pelos informados; campos opcionais deixados em branco são apagados.")
    @ApiResponse(responseCode = "200", description = "Cidade atualizado")
    @ApiResponse(responseCode = "400", description = "Dados inválidos: o nome (até 100 caracteres) e o estado são obrigatórios",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "404", description = "O cidade não existe, ou referência inexistente (o estado informado não existe)",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    public CidadeResponse atualizar(@Parameter(description = "Escolha na lista (cidade)") @PathVariable String id,
                                  @Valid @ParameterObject CidadeRequest request) {
        return service.atualizar(OpcaoSelecionada.id(id, "id"), request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir cidade",
            description = "Remove definitivamente o cidade. Só é permitido se nenhum outro cadastro estiver usando este registro.")
    @ApiResponse(responseCode = "204", description = "Cidade excluído")
    @ApiResponse(responseCode = "404", description = "O cidade não existe",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "409", description = "Não é possível excluir: a cidade está em uso por algum cliente ou fornecedor",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    public void excluir(@Parameter(description = "Escolha na lista (cidade)") @PathVariable String id) {
        service.excluir(OpcaoSelecionada.id(id, "id"));
    }
}
