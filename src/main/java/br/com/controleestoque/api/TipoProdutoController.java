package br.com.controleestoque.api;

import br.com.controleestoque.api.handler.ErroResponse;
import br.com.controleestoque.api.request.TipoProdutoRequest;
import br.com.controleestoque.api.response.TipoProdutoResponse;
import br.com.controleestoque.service.OpcaoSelecionada;
import br.com.controleestoque.service.TipoProdutoService;
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
@RequestMapping("/api/tipos-produto")
@RequiredArgsConstructor
@Tag(name = "Tipos de Produto", description = "Classificação do produto quanto à natureza (ex.: matéria-prima, produto acabado, revenda).")
public class TipoProdutoController {

    private final TipoProdutoService service;

    @GetMapping
    @Operation(summary = "Listar tipos de produto",
            description = "Retorna a lista de tipos de produto cadastrados.")
    @ApiResponse(responseCode = "200", description = "Lista retornada (vazia se não houver registros)")
    public List<TipoProdutoResponse> listar() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar tipo de produto por id",
            description = "Retorna os dados de um tipo de produto a partir do seu id.")
    @ApiResponse(responseCode = "200", description = "Tipo de produto encontrado")
    @ApiResponse(responseCode = "404", description = "O tipo de produto não existe",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    public TipoProdutoResponse buscar(@Parameter(description = "Escolha na lista (tipo de produto)") @PathVariable String id) {
        return service.buscar(OpcaoSelecionada.id(id, "id"));
    }

    @PostMapping
    @Operation(summary = "Cadastrar tipo de produto",
            description = "Cria um novo tipo de produto: preencha os campos abaixo (os marcados com * são obrigatórios) e clique em Execute. Devolve o registro criado com o seu id.")
    @ApiResponse(responseCode = "201", description = "Tipo de produto criado; o cabeçalho Location aponta para o novo recurso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos: a descrição é obrigatória e deve ter no máximo 100 caracteres",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    public ResponseEntity<TipoProdutoResponse> criar(@Valid @ParameterObject TipoProdutoRequest request) {
        TipoProdutoResponse criado = service.criar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{id}").buildAndExpand(criado.idTipoProduto()).toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar tipo de produto",
            description = "Altera o tipo de produto: informe o id e preencha os campos abaixo (os marcados com * são obrigatórios). Os dados atuais são substituídos pelos informados; campos opcionais deixados em branco são apagados.")
    @ApiResponse(responseCode = "200", description = "Tipo de produto atualizado")
    @ApiResponse(responseCode = "400", description = "Dados inválidos: a descrição é obrigatória e deve ter no máximo 100 caracteres",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "404", description = "O tipo de produto não existe",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    public TipoProdutoResponse atualizar(@Parameter(description = "Escolha na lista (tipo de produto)") @PathVariable String id,
                                  @Valid @ParameterObject TipoProdutoRequest request) {
        return service.atualizar(OpcaoSelecionada.id(id, "id"), request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir tipo de produto",
            description = "Remove definitivamente o tipo de produto. Só é permitido se nenhum outro cadastro estiver usando este registro.")
    @ApiResponse(responseCode = "204", description = "Tipo de produto excluído")
    @ApiResponse(responseCode = "404", description = "O tipo de produto não existe",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "409", description = "Não é possível excluir: o tipo de produto está em uso por algum produto",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    public void excluir(@Parameter(description = "Escolha na lista (tipo de produto)") @PathVariable String id) {
        service.excluir(OpcaoSelecionada.id(id, "id"));
    }
}
