package br.com.controleestoque.api;

import br.com.controleestoque.api.handler.ErroResponse;
import br.com.controleestoque.api.response.EstadoResponse;
import br.com.controleestoque.service.EstadoService;
import br.com.controleestoque.service.OpcaoSelecionada;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/estados")
@RequiredArgsConstructor
@Tag(name = "Estados", description = "Estados brasileiros (27 UFs), carregados pela migration. Somente leitura; usados para cadastrar cidades.")
public class EstadoController {

    private final EstadoService service;

    @GetMapping
    @Operation(summary = "Listar estados",
            description = "Retorna os 27 estados brasileiros, ordenados por nome.")
    @ApiResponse(responseCode = "200", description = "Lista de estados")
    public List<EstadoResponse> listar() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar estado por id",
            description = "Retorna os dados de um estado (nome e UF) a partir do seu id.")
    @ApiResponse(responseCode = "200", description = "Estado encontrado")
    @ApiResponse(responseCode = "404", description = "O estado não existe",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    public EstadoResponse buscar(@Parameter(description = "Escolha na lista (estado)") @PathVariable String id) {
        return service.buscar(OpcaoSelecionada.id(id, "id"));
    }
}
