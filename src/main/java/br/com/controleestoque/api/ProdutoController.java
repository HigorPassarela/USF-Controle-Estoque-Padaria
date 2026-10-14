package br.com.controleestoque.api;

import br.com.controleestoque.api.handler.ErroResponse;
import br.com.controleestoque.api.request.ProdutoRequest;
import br.com.controleestoque.api.response.ProdutoResponse;
import br.com.controleestoque.service.OpcaoSelecionada;
import br.com.controleestoque.service.ProdutoService;
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
@RequestMapping("/api/produtos")
@RequiredArgsConstructor
@Tag(name = "Produtos", description = "Cadastro de produtos vendidos e estocados. O saldo em estoque é somente leitura aqui: nasce em 0 e só muda por movimentações de estoque. A exclusão é lógica.")
public class ProdutoController {

    private final ProdutoService service;

    @GetMapping
    @Operation(summary = "Listar produtos",
            description = "Retorna a lista de produtos cadastrados. Retorna apenas os registros ativos; os excluídos não aparecem.")
    @ApiResponse(responseCode = "200", description = "Lista retornada (vazia se não houver registros)")
    public List<ProdutoResponse> listar() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar produto por id",
            description = "Retorna os dados de um produto a partir do seu id.")
    @ApiResponse(responseCode = "200", description = "Produto encontrado")
    @ApiResponse(responseCode = "404", description = "O produto não existe ou está inativo",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    public ProdutoResponse buscar(@Parameter(description = "Escolha na lista (produto)") @PathVariable String id) {
        return service.buscar(OpcaoSelecionada.id(id, "id"));
    }

    @PostMapping
    @Operation(summary = "Cadastrar produto",
            description = "Cria um novo produto: preencha os campos abaixo (os marcados com * são obrigatórios) e clique em Execute. Devolve o registro criado com o seu id. O registro nasce ativo.")
    @ApiResponse(responseCode = "201", description = "Produto criado; o cabeçalho Location aponta para o novo recurso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos: nome, categoria, tipo e unidade de medida são obrigatórios e o preço de venda deve ser maior que zero",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "404", description = "Referência inexistente: a categoria, o tipo, a unidade de medida ou o fornecedor (ativo) informados não existem",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    public ResponseEntity<ProdutoResponse> criar(@Valid @ParameterObject ProdutoRequest request) {
        ProdutoResponse criado = service.criar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{id}").buildAndExpand(criado.idProduto()).toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar produto",
            description = "Altera o produto: informe o id e preencha os campos abaixo (os marcados com * são obrigatórios). Os dados atuais são substituídos pelos informados; campos opcionais deixados em branco são apagados. O saldo em estoque não é alterado por esta operação.")
    @ApiResponse(responseCode = "200", description = "Produto atualizado")
    @ApiResponse(responseCode = "400", description = "Dados inválidos: nome, categoria, tipo e unidade de medida são obrigatórios e o preço de venda deve ser maior que zero",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "404", description = "O produto não existe ou está inativo, ou referência inexistente (a categoria, o tipo, a unidade de medida ou o fornecedor (ativo) informados não existem)",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    public ProdutoResponse atualizar(@Parameter(description = "Escolha na lista (produto)") @PathVariable String id,
                                  @Valid @ParameterObject ProdutoRequest request) {
        return service.atualizar(OpcaoSelecionada.id(id, "id"), request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir produto",
            description = "Exclusão lógica: marca o produto como inativo. Ele deixa de aparecer nas listagens e nas buscas (404), mas o registro e o histórico são preservados.")
    @ApiResponse(responseCode = "204", description = "Produto excluído (inativado)")
    @ApiResponse(responseCode = "404", description = "O produto não existe ou já está inativo",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    public void excluir(@Parameter(description = "Escolha na lista (produto)") @PathVariable String id) {
        service.excluir(OpcaoSelecionada.id(id, "id"));
    }
}
