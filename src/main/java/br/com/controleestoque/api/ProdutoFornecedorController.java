package br.com.controleestoque.api;

import br.com.controleestoque.api.handler.ErroResponse;
import br.com.controleestoque.api.request.CustoUnitarioRequest;
import br.com.controleestoque.api.request.ProdutoFornecedorRequest;
import br.com.controleestoque.api.response.ProdutoFornecedorResponse;
import br.com.controleestoque.service.OpcaoSelecionada;
import br.com.controleestoque.service.ProdutoFornecedorService;
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
@RequestMapping("/api/produtos/{id}/fornecedores")
@RequiredArgsConstructor
@Tag(name = "Fornecedores do Produto", description = "Quais fornecedores fornecem cada produto e por qual custo unitário. O custo do vínculo é usado como padrão nas entradas de estoque.")
public class ProdutoFornecedorController {

    private final ProdutoFornecedorService service;

    @GetMapping
    @Operation(summary = "Listar fornecedores do produto",
            description = "Retorna os fornecedores ativos vinculados ao produto, com o custo unitário de cada um.")
    @ApiResponse(responseCode = "200", description = "Lista retornada (vazia se o produto não tiver fornecedores)")
    @ApiResponse(responseCode = "404", description = "O produto não existe ou está inativo",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    public List<ProdutoFornecedorResponse> listar(@Parameter(description = "Escolha na lista (produto)") @PathVariable String id) {
        return service.listar(OpcaoSelecionada.id(id, "id"));
    }

    @PostMapping
    @Operation(summary = "Vincular fornecedor ao produto",
            description = "Vincula um fornecedor ativo ao produto com o custo unitário de compra. Cada fornecedor só pode ser vinculado uma vez ao mesmo produto.")
    @ApiResponse(responseCode = "201", description = "Vínculo criado; o cabeçalho Location aponta para ele")
    @ApiResponse(responseCode = "400", description = "Dados inválidos: o fornecedor é obrigatório e o custo unitário deve ser zero ou mais, com até 2 casas decimais",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "404", description = "O produto não existe ou está inativo, ou o fornecedor não existe ou está inativo",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "409", description = "Conflito: o fornecedor já está vinculado ao produto",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    public ResponseEntity<ProdutoFornecedorResponse> vincular(@Parameter(description = "Escolha na lista (produto)") @PathVariable String id,
                                                              @Valid @ParameterObject ProdutoFornecedorRequest request) {
        ProdutoFornecedorResponse criado = service.vincular(OpcaoSelecionada.id(id, "id"), request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{idFornecedor}").buildAndExpand(criado.idFornecedor()).toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @PutMapping("/{idFornecedor}")
    @Operation(summary = "Alterar custo do fornecedor",
            description = "Altera o custo unitário de um fornecedor já vinculado ao produto.")
    @ApiResponse(responseCode = "200", description = "Custo atualizado")
    @ApiResponse(responseCode = "400", description = "Dados inválidos: o custo unitário deve ser zero ou mais, com até 2 casas decimais",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "404", description = "O produto não existe ou está inativo, ou o fornecedor não está vinculado a ele",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    public ProdutoFornecedorResponse atualizarCusto(@Parameter(description = "Escolha na lista (produto)") @PathVariable String id,
                                                    @Parameter(description = "Escolha na lista (fornecedor)") @PathVariable String idFornecedor,
                                                    @Valid @ParameterObject CustoUnitarioRequest request) {
        return service.atualizarCusto(OpcaoSelecionada.id(id, "id"), OpcaoSelecionada.id(idFornecedor, "idFornecedor"), request);
    }

    @DeleteMapping("/{idFornecedor}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Desvincular fornecedor do produto",
            description = "Remove o vínculo. Lotes já recebidos desse fornecedor não são alterados.")
    @ApiResponse(responseCode = "204", description = "Vínculo removido")
    @ApiResponse(responseCode = "404", description = "O produto não existe ou está inativo, ou o fornecedor não está vinculado a ele",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    public void desvincular(@Parameter(description = "Escolha na lista (produto)") @PathVariable String id,
                            @Parameter(description = "Escolha na lista (fornecedor)") @PathVariable String idFornecedor) {
        service.desvincular(OpcaoSelecionada.id(id, "id"), OpcaoSelecionada.id(idFornecedor, "idFornecedor"));
    }
}
