package br.com.controleestoque.api;

import br.com.controleestoque.api.handler.ErroResponse;
import br.com.controleestoque.api.request.UnidadeMedidaRequest;
import br.com.controleestoque.api.response.UnidadeMedidaResponse;
import br.com.controleestoque.service.OpcaoSelecionada;
import br.com.controleestoque.service.UnidadeMedidaService;
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
@RequestMapping("/api/unidades-medida")
@RequiredArgsConstructor
@Tag(name = "Unidades de Medida", description = "Unidades em que os produtos são medidos e vendidos (ex.: quilo/kg, unidade/un, litro/L).")
public class UnidadeMedidaController {

    private final UnidadeMedidaService service;

    @GetMapping
    @Operation(summary = "Listar unidades de medida",
            description = "Retorna a lista de unidades de medida cadastrados.")
    @ApiResponse(responseCode = "200", description = "Lista retornada (vazia se não houver registros)")
    public List<UnidadeMedidaResponse> listar() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar unidade de medida por id",
            description = "Retorna os dados de um unidade de medida a partir do seu id.")
    @ApiResponse(responseCode = "200", description = "Unidade de medida encontrado")
    @ApiResponse(responseCode = "404", description = "O unidade de medida não existe",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    public UnidadeMedidaResponse buscar(@Parameter(description = "Escolha na lista (unidade de medida)") @PathVariable String id) {
        return service.buscar(OpcaoSelecionada.id(id, "id"));
    }

    @PostMapping
    @Operation(summary = "Cadastrar unidade de medida",
            description = "Cria um novo unidade de medida: preencha os campos abaixo (os marcados com * são obrigatórios) e clique em Execute. Devolve o registro criado com o seu id.")
    @ApiResponse(responseCode = "201", description = "Unidade de medida criado; o cabeçalho Location aponta para o novo recurso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos: a descrição (até 100 caracteres) e a sigla (até 10 caracteres) são obrigatórias",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    public ResponseEntity<UnidadeMedidaResponse> criar(@Valid @ParameterObject UnidadeMedidaRequest request) {
        UnidadeMedidaResponse criado = service.criar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{id}").buildAndExpand(criado.idUnidadeMedida()).toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar unidade de medida",
            description = "Altera o unidade de medida: informe o id e preencha os campos abaixo (os marcados com * são obrigatórios). Os dados atuais são substituídos pelos informados; campos opcionais deixados em branco são apagados.")
    @ApiResponse(responseCode = "200", description = "Unidade de medida atualizado")
    @ApiResponse(responseCode = "400", description = "Dados inválidos: a descrição (até 100 caracteres) e a sigla (até 10 caracteres) são obrigatórias",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "404", description = "O unidade de medida não existe",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    public UnidadeMedidaResponse atualizar(@Parameter(description = "Escolha na lista (unidade de medida)") @PathVariable String id,
                                  @Valid @ParameterObject UnidadeMedidaRequest request) {
        return service.atualizar(OpcaoSelecionada.id(id, "id"), request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir unidade de medida",
            description = "Remove definitivamente o unidade de medida. Só é permitido se nenhum outro cadastro estiver usando este registro.")
    @ApiResponse(responseCode = "204", description = "Unidade de medida excluído")
    @ApiResponse(responseCode = "404", description = "O unidade de medida não existe",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "409", description = "Não é possível excluir: a unidade de medida está em uso por algum produto",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    public void excluir(@Parameter(description = "Escolha na lista (unidade de medida)") @PathVariable String id) {
        service.excluir(OpcaoSelecionada.id(id, "id"));
    }
}
