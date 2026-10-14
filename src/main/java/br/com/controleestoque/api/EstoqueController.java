package br.com.controleestoque.api;

import br.com.controleestoque.api.handler.ErroResponse;
import br.com.controleestoque.api.request.EntradaEstoqueRequest;
import br.com.controleestoque.api.request.LoteFiltro;
import br.com.controleestoque.api.request.MovimentacaoFiltro;
import br.com.controleestoque.api.request.SaidaEstoqueRequest;
import br.com.controleestoque.api.request.SaidaLoteRequest;
import br.com.controleestoque.api.response.EntradaEstoqueResponse;
import br.com.controleestoque.api.response.LoteResponse;
import br.com.controleestoque.api.response.MovimentacaoEstoqueResponse;
import br.com.controleestoque.api.response.SaidaEstoqueResponse;
import br.com.controleestoque.api.response.SaldoProdutoResponse;
import br.com.controleestoque.service.EstoqueService;
import br.com.controleestoque.service.OpcaoSelecionada;
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
@RequestMapping("/api/estoque")
@RequiredArgsConstructor
@Tag(name = "Estoque", description = "Estoque por lote: entradas com validade, saídas com baixa FEFO (vence primeiro, sai primeiro) "
        + "sem consumir lotes vencidos, descarte de lote, histórico de movimentações e saldos por produto. "
        + "O saldo do produto é sempre a soma dos saldos dos seus lotes.")
public class EstoqueController {

    private final EstoqueService service;

    @PostMapping("/entradas")
    @Operation(summary = "Registrar entrada de estoque",
            description = "Cria um lote do produto com a quantidade recebida, registra a movimentação de entrada e soma a quantidade ao saldo do produto. "
                    + "Se o fornecedor estiver vinculado ao produto e o custo ficar em branco, o lote usa o custo do vínculo.")
    @ApiResponse(responseCode = "201", description = "Entrada registrada; o cabeçalho Location aponta para o lote criado")
    @ApiResponse(responseCode = "400", description = "Dados inválidos: quantidade maior que zero com até 3 casas, validade não anterior a hoje e tipo de movimentação de ENTRADA",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "404", description = "O produto, o fornecedor (ativos) ou o tipo de movimentação não existem",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    public ResponseEntity<EntradaEstoqueResponse> registrarEntrada(@Valid @ParameterObject EntradaEstoqueRequest request) {
        EntradaEstoqueResponse entrada = service.registrarEntrada(request);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/estoque/lotes/{id}").buildAndExpand(entrada.lote().idLote()).toUri();
        return ResponseEntity.created(location).body(entrada);
    }

    @PostMapping("/saidas")
    @Operation(summary = "Registrar saída de estoque (FEFO)",
            description = "Dá baixa na quantidade consumindo primeiro os lotes que vencem antes (lotes sem validade por último). "
                    + "Lotes vencidos não são consumidos: use a saída de lote específico para descartá-los. "
                    + "Se o saldo disponível não bastar, nada é alterado.")
    @ApiResponse(responseCode = "201", description = "Saída registrada; devolve uma movimentação por lote afetado e o novo saldo")
    @ApiResponse(responseCode = "400", description = "Dados inválidos: quantidade maior que zero com até 3 casas e tipo de movimentação de SAIDA",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "404", description = "O produto (ativo) ou o tipo de movimentação não existem",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "409", description = "Estoque insuficiente: o saldo disponível (fora lotes vencidos) é menor que a quantidade pedida",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    public ResponseEntity<SaidaEstoqueResponse> registrarSaida(@Valid @ParameterObject SaidaEstoqueRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrarSaida(request));
    }

    @GetMapping("/lotes")
    @Operation(summary = "Listar lotes",
            description = "Lista os lotes, do que vence antes para o que vence depois (sem validade por último). Todos os filtros são opcionais.")
    @ApiResponse(responseCode = "200", description = "Lista retornada (vazia se nenhum lote atender aos filtros)")
    public List<LoteResponse> listarLotes(@ParameterObject LoteFiltro filtro) {
        return service.listarLotes(filtro);
    }

    @GetMapping("/lotes/{id}")
    @Operation(summary = "Buscar lote por id",
            description = "Retorna os dados de um lote, incluindo o saldo atual e se está vencido.")
    @ApiResponse(responseCode = "200", description = "Lote encontrado")
    @ApiResponse(responseCode = "404", description = "O lote não existe",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    public LoteResponse buscarLote(@Parameter(description = "Escolha na lista (lote)") @PathVariable String id) {
        return service.buscarLote(OpcaoSelecionada.id(id, "id"));
    }

    @PostMapping("/lotes/{id}/saidas")
    @Operation(summary = "Registrar saída de um lote específico",
            description = "Dá baixa apenas no lote escolhido, inclusive se estiver vencido. Use para perda ou descarte.")
    @ApiResponse(responseCode = "201", description = "Saída registrada; devolve a movimentação e o novo saldo do produto")
    @ApiResponse(responseCode = "400", description = "Dados inválidos: quantidade maior que zero com até 3 casas e tipo de movimentação de SAIDA",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "404", description = "O lote ou o tipo de movimentação não existem",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "409", description = "Saldo insuficiente: a quantidade é maior que o saldo do lote",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    public ResponseEntity<SaidaEstoqueResponse> registrarSaidaDoLote(@Parameter(description = "Escolha na lista (lote)") @PathVariable String id,
                                                                     @Valid @ParameterObject SaidaLoteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.registrarSaidaDoLote(OpcaoSelecionada.id(id, "id"), request));
    }

    @GetMapping("/movimentacoes")
    @Operation(summary = "Listar movimentações",
            description = "Histórico de entradas e saídas, da mais recente para a mais antiga. Movimentações não podem ser alteradas nem excluídas. Todos os filtros são opcionais.")
    @ApiResponse(responseCode = "200", description = "Lista retornada (vazia se nenhuma movimentação atender aos filtros)")
    @ApiResponse(responseCode = "400", description = "Filtro inválido: datas no formato AAAA-MM-DD e data final não anterior à inicial",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    public List<MovimentacaoEstoqueResponse> listarMovimentacoes(@ParameterObject MovimentacaoFiltro filtro) {
        return service.listarMovimentacoes(filtro);
    }

    @GetMapping("/movimentacoes/{id}")
    @Operation(summary = "Buscar movimentação por id",
            description = "Retorna uma movimentação de estoque. Movimentações não podem ser alteradas nem excluídas.")
    @ApiResponse(responseCode = "200", description = "Movimentação encontrada")
    @ApiResponse(responseCode = "404", description = "A movimentação não existe",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    public MovimentacaoEstoqueResponse buscarMovimentacao(@Parameter(description = "Id da movimentação") @PathVariable String id) {
        return service.buscarMovimentacao(OpcaoSelecionada.id(id, "id"));
    }

    @GetMapping("/saldos")
    @Operation(summary = "Listar saldos por produto",
            description = "Para cada produto ativo: saldo total, disponível (lotes não vencidos), vencido e a data do próximo vencimento.")
    @ApiResponse(responseCode = "200", description = "Saldos retornados")
    public List<SaldoProdutoResponse> listarSaldos() {
        return service.listarSaldos();
    }

    @GetMapping("/saldos/{idProduto}")
    @Operation(summary = "Buscar saldo de um produto",
            description = "Saldo total, disponível (lotes não vencidos), vencido e a data do próximo vencimento do produto.")
    @ApiResponse(responseCode = "200", description = "Saldo retornado (zerado se o produto nunca recebeu entrada)")
    @ApiResponse(responseCode = "404", description = "O produto não existe ou está inativo",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    public SaldoProdutoResponse buscarSaldo(@Parameter(description = "Escolha na lista (produto)") @PathVariable String idProduto) {
        return service.buscarSaldo(OpcaoSelecionada.id(idProduto, "idProduto"));
    }
}
