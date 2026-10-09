package br.com.controleestoque.api;

import br.com.controleestoque.api.handler.ErroResponse;
import br.com.controleestoque.api.request.TipoClienteRequest;
import br.com.controleestoque.api.response.TipoClienteResponse;
import br.com.controleestoque.service.OpcaoSelecionada;
import br.com.controleestoque.service.TipoClienteService;
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
@RequestMapping("/api/tipos-cliente")
@RequiredArgsConstructor
@Tag(name = "Tipos de Cliente", description = "Classificação dos clientes (ex.: pessoa física, pessoa jurídica).")
public class TipoClienteController {

    private final TipoClienteService service;

    @GetMapping
    @Operation(summary = "Listar tipos de cliente",
            description = "Retorna a lista de tipos de cliente cadastrados.")
    @ApiResponse(responseCode = "200", description = "Lista retornada (vazia se não houver registros)")
    public List<TipoClienteResponse> listar() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar tipo de cliente por id",
            description = "Retorna os dados de um tipo de cliente a partir do seu id.")
    @ApiResponse(responseCode = "200", description = "Tipo de cliente encontrado")
    @ApiResponse(responseCode = "404", description = "O tipo de cliente não existe",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    public TipoClienteResponse buscar(@Parameter(description = "Escolha na lista (tipo de cliente)") @PathVariable String id) {
        return service.buscar(OpcaoSelecionada.id(id, "id"));
    }

    @PostMapping
    @Operation(summary = "Cadastrar tipo de cliente",
            description = "Cria um novo tipo de cliente: preencha os campos abaixo (os marcados com * são obrigatórios) e clique em Execute. Devolve o registro criado com o seu id.")
    @ApiResponse(responseCode = "201", description = "Tipo de cliente criado; o cabeçalho Location aponta para o novo recurso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos: a descrição é obrigatória e deve ter no máximo 100 caracteres",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    public ResponseEntity<TipoClienteResponse> criar(@Valid @ParameterObject TipoClienteRequest request) {
        TipoClienteResponse criado = service.criar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{id}").buildAndExpand(criado.idTipoCliente()).toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar tipo de cliente",
            description = "Altera o tipo de cliente: informe o id e preencha os campos abaixo (os marcados com * são obrigatórios). Os dados atuais são substituídos pelos informados; campos opcionais deixados em branco são apagados.")
    @ApiResponse(responseCode = "200", description = "Tipo de cliente atualizado")
    @ApiResponse(responseCode = "400", description = "Dados inválidos: a descrição é obrigatória e deve ter no máximo 100 caracteres",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "404", description = "O tipo de cliente não existe",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    public TipoClienteResponse atualizar(@Parameter(description = "Escolha na lista (tipo de cliente)") @PathVariable String id,
                                  @Valid @ParameterObject TipoClienteRequest request) {
        return service.atualizar(OpcaoSelecionada.id(id, "id"), request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir tipo de cliente",
            description = "Remove definitivamente o tipo de cliente. Só é permitido se nenhum outro cadastro estiver usando este registro.")
    @ApiResponse(responseCode = "204", description = "Tipo de cliente excluído")
    @ApiResponse(responseCode = "404", description = "O tipo de cliente não existe",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "409", description = "Não é possível excluir: o tipo de cliente está em uso por algum cliente",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    public void excluir(@Parameter(description = "Escolha na lista (tipo de cliente)") @PathVariable String id) {
        service.excluir(OpcaoSelecionada.id(id, "id"));
    }
}
