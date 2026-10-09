package br.com.controleestoque.api;

import br.com.controleestoque.api.handler.ErroResponse;
import br.com.controleestoque.api.request.TipoMovimentacaoEstoqueRequest;
import br.com.controleestoque.api.response.TipoMovimentacaoEstoqueResponse;
import br.com.controleestoque.service.OpcaoSelecionada;
import br.com.controleestoque.service.TipoMovimentacaoEstoqueService;
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
@RequestMapping("/api/tipos-movimentacao")
@RequiredArgsConstructor
@Tag(name = "Tipos de Movimentação de Estoque", description = "Tipos de movimentação do estoque (ex.: compra, venda, perda), cada um classificado como ENTRADA ou SAIDA.")
public class TipoMovimentacaoEstoqueController {

    private final TipoMovimentacaoEstoqueService service;

    @GetMapping
    @Operation(summary = "Listar tipos de movimentação de estoque",
            description = "Retorna a lista de tipos de movimentação de estoque cadastrados.")
    @ApiResponse(responseCode = "200", description = "Lista retornada (vazia se não houver registros)")
    public List<TipoMovimentacaoEstoqueResponse> listar() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar tipo de movimentação de estoque por id",
            description = "Retorna os dados de um tipo de movimentação de estoque a partir do seu id.")
    @ApiResponse(responseCode = "200", description = "Tipo de movimentação de estoque encontrado")
    @ApiResponse(responseCode = "404", description = "O tipo de movimentação de estoque não existe",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    public TipoMovimentacaoEstoqueResponse buscar(@Parameter(description = "Escolha na lista (tipo de movimentação de estoque)") @PathVariable String id) {
        return service.buscar(OpcaoSelecionada.id(id, "id"));
    }

    @PostMapping
    @Operation(summary = "Cadastrar tipo de movimentação de estoque",
            description = "Cria um novo tipo de movimentação de estoque: preencha os campos abaixo (os marcados com * são obrigatórios) e clique em Execute. Devolve o registro criado com o seu id.")
    @ApiResponse(responseCode = "201", description = "Tipo de movimentação de estoque criado; o cabeçalho Location aponta para o novo recurso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos: a descrição (até 100 caracteres) e o tipo (até 50 caracteres, ex.: ENTRADA ou SAIDA) são obrigatórios",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    public ResponseEntity<TipoMovimentacaoEstoqueResponse> criar(@Valid @ParameterObject TipoMovimentacaoEstoqueRequest request) {
        TipoMovimentacaoEstoqueResponse criado = service.criar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{id}").buildAndExpand(criado.idTipoMovimentacao()).toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar tipo de movimentação de estoque",
            description = "Altera o tipo de movimentação de estoque: informe o id e preencha os campos abaixo (os marcados com * são obrigatórios). Os dados atuais são substituídos pelos informados; campos opcionais deixados em branco são apagados.")
    @ApiResponse(responseCode = "200", description = "Tipo de movimentação de estoque atualizado")
    @ApiResponse(responseCode = "400", description = "Dados inválidos: a descrição (até 100 caracteres) e o tipo (até 50 caracteres, ex.: ENTRADA ou SAIDA) são obrigatórios",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "404", description = "O tipo de movimentação de estoque não existe",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    public TipoMovimentacaoEstoqueResponse atualizar(@Parameter(description = "Escolha na lista (tipo de movimentação de estoque)") @PathVariable String id,
                                  @Valid @ParameterObject TipoMovimentacaoEstoqueRequest request) {
        return service.atualizar(OpcaoSelecionada.id(id, "id"), request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir tipo de movimentação de estoque",
            description = "Remove definitivamente o tipo de movimentação de estoque. Só é permitido se nenhum outro cadastro estiver usando este registro.")
    @ApiResponse(responseCode = "204", description = "Tipo de movimentação de estoque excluído")
    @ApiResponse(responseCode = "404", description = "O tipo de movimentação de estoque não existe",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "409", description = "Não é possível excluir: o tipo de movimentação está em uso por alguma movimentação",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    public void excluir(@Parameter(description = "Escolha na lista (tipo de movimentação de estoque)") @PathVariable String id) {
        service.excluir(OpcaoSelecionada.id(id, "id"));
    }
}
