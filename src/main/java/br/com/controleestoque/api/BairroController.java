package br.com.controleestoque.api;

import br.com.controleestoque.api.handler.ErroResponse;
import br.com.controleestoque.api.request.BairroRequest;
import br.com.controleestoque.api.response.BairroResponse;
import br.com.controleestoque.service.OpcaoSelecionada;
import br.com.controleestoque.service.BairroService;
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
@RequestMapping("/api/bairros")
@RequiredArgsConstructor
@Tag(name = "Bairros", description = "Bairros usados no endereço de clientes e fornecedores. Cada bairro pertence a uma cidade.")
public class BairroController {

    private final BairroService service;

    @GetMapping
    @Operation(summary = "Listar bairros",
            description = "Retorna a lista de bairros cadastrados.")
    @ApiResponse(responseCode = "200", description = "Lista retornada (vazia se não houver registros)")
    public List<BairroResponse> listar() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar bairro por id",
            description = "Retorna os dados de um bairro a partir do seu id.")
    @ApiResponse(responseCode = "200", description = "Bairro encontrado")
    @ApiResponse(responseCode = "404", description = "O bairro não existe",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    public BairroResponse buscar(@Parameter(description = "Escolha na lista (bairro)") @PathVariable String id) {
        return service.buscar(OpcaoSelecionada.id(id, "id"));
    }

    @PostMapping
    @Operation(summary = "Cadastrar bairro",
            description = "Cria um novo bairro: preencha os campos abaixo (os marcados com * são obrigatórios) e clique em Execute. Devolve o registro criado com o seu id.")
    @ApiResponse(responseCode = "201", description = "Bairro criado; o cabeçalho Location aponta para o novo recurso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos: o nome (até 100 caracteres) e a cidade são obrigatórios",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "404", description = "Referência inexistente: a cidade informada não existe",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    public ResponseEntity<BairroResponse> criar(@Valid @ParameterObject BairroRequest request) {
        BairroResponse criado = service.criar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{id}").buildAndExpand(criado.idBairro()).toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar bairro",
            description = "Altera o bairro: informe o id e preencha os campos abaixo (os marcados com * são obrigatórios). Os dados atuais são substituídos pelos informados; campos opcionais deixados em branco são apagados.")
    @ApiResponse(responseCode = "200", description = "Bairro atualizado")
    @ApiResponse(responseCode = "400", description = "Dados inválidos: o nome (até 100 caracteres) e a cidade são obrigatórios",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "404", description = "O bairro não existe, ou referência inexistente (a cidade informada não existe)",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    public BairroResponse atualizar(@Parameter(description = "Escolha na lista (bairro)") @PathVariable String id,
                                  @Valid @ParameterObject BairroRequest request) {
        return service.atualizar(OpcaoSelecionada.id(id, "id"), request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir bairro",
            description = "Remove definitivamente o bairro. Só é permitido se nenhum outro cadastro estiver usando este registro.")
    @ApiResponse(responseCode = "204", description = "Bairro excluído")
    @ApiResponse(responseCode = "404", description = "O bairro não existe",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "409", description = "Não é possível excluir: o bairro está em uso por algum cliente ou fornecedor",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    public void excluir(@Parameter(description = "Escolha na lista (bairro)") @PathVariable String id) {
        service.excluir(OpcaoSelecionada.id(id, "id"));
    }
}
