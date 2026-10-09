package br.com.controleestoque.api;

import br.com.controleestoque.api.handler.ErroResponse;
import br.com.controleestoque.api.request.CategoriaProdutoRequest;
import br.com.controleestoque.api.response.CategoriaProdutoResponse;
import br.com.controleestoque.service.OpcaoSelecionada;
import br.com.controleestoque.service.CategoriaProdutoService;
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
@RequestMapping("/api/categorias-produto")
@RequiredArgsConstructor
@Tag(name = "Categorias de Produto", description = "Agrupamento de produtos para organização e consulta (ex.: pães, doces, bebidas).")
public class CategoriaProdutoController {

    private final CategoriaProdutoService service;

    @GetMapping
    @Operation(summary = "Listar categorias de produto",
            description = "Retorna a lista de categorias de produto cadastrados.")
    @ApiResponse(responseCode = "200", description = "Lista retornada (vazia se não houver registros)")
    public List<CategoriaProdutoResponse> listar() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar categoria de produto por id",
            description = "Retorna os dados de um categoria de produto a partir do seu id.")
    @ApiResponse(responseCode = "200", description = "Categoria de produto encontrado")
    @ApiResponse(responseCode = "404", description = "O categoria de produto não existe",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    public CategoriaProdutoResponse buscar(@Parameter(description = "Escolha na lista (categoria de produto)") @PathVariable String id) {
        return service.buscar(OpcaoSelecionada.id(id, "id"));
    }

    @PostMapping
    @Operation(summary = "Cadastrar categoria de produto",
            description = "Cria um novo categoria de produto: preencha os campos abaixo (os marcados com * são obrigatórios) e clique em Execute. Devolve o registro criado com o seu id.")
    @ApiResponse(responseCode = "201", description = "Categoria de produto criado; o cabeçalho Location aponta para o novo recurso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos: a descrição é obrigatória e deve ter no máximo 100 caracteres",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    public ResponseEntity<CategoriaProdutoResponse> criar(@Valid @ParameterObject CategoriaProdutoRequest request) {
        CategoriaProdutoResponse criado = service.criar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{id}").buildAndExpand(criado.idCategoriaProduto()).toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar categoria de produto",
            description = "Altera o categoria de produto: informe o id e preencha os campos abaixo (os marcados com * são obrigatórios). Os dados atuais são substituídos pelos informados; campos opcionais deixados em branco são apagados.")
    @ApiResponse(responseCode = "200", description = "Categoria de produto atualizado")
    @ApiResponse(responseCode = "400", description = "Dados inválidos: a descrição é obrigatória e deve ter no máximo 100 caracteres",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "404", description = "O categoria de produto não existe",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    public CategoriaProdutoResponse atualizar(@Parameter(description = "Escolha na lista (categoria de produto)") @PathVariable String id,
                                  @Valid @ParameterObject CategoriaProdutoRequest request) {
        return service.atualizar(OpcaoSelecionada.id(id, "id"), request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir categoria de produto",
            description = "Remove definitivamente o categoria de produto. Só é permitido se nenhum outro cadastro estiver usando este registro.")
    @ApiResponse(responseCode = "204", description = "Categoria de produto excluído")
    @ApiResponse(responseCode = "404", description = "O categoria de produto não existe",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "409", description = "Não é possível excluir: a categoria está em uso por algum produto",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    public void excluir(@Parameter(description = "Escolha na lista (categoria de produto)") @PathVariable String id) {
        service.excluir(OpcaoSelecionada.id(id, "id"));
    }
}
