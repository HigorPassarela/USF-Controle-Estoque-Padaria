package br.com.controleestoque.api;

import br.com.controleestoque.api.handler.ErroResponse;
import br.com.controleestoque.api.request.MetodoPagamentoRequest;
import br.com.controleestoque.api.response.MetodoPagamentoResponse;
import br.com.controleestoque.service.OpcaoSelecionada;
import br.com.controleestoque.service.MetodoPagamentoService;
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
@RequestMapping("/api/metodos-pagamento")
@RequiredArgsConstructor
@Tag(name = "Métodos de Pagamento", description = "Formas de pagamento aceitas pela padaria (ex.: dinheiro, Pix, cartão).")
public class MetodoPagamentoController {

    private final MetodoPagamentoService service;

    @GetMapping
    @Operation(summary = "Listar métodos de pagamento",
            description = "Retorna a lista de métodos de pagamento cadastrados.")
    @ApiResponse(responseCode = "200", description = "Lista retornada (vazia se não houver registros)")
    public List<MetodoPagamentoResponse> listar() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar método de pagamento por id",
            description = "Retorna os dados de um método de pagamento a partir do seu id.")
    @ApiResponse(responseCode = "200", description = "Método de pagamento encontrado")
    @ApiResponse(responseCode = "404", description = "O método de pagamento não existe",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    public MetodoPagamentoResponse buscar(@Parameter(description = "Escolha na lista (método de pagamento)") @PathVariable String id) {
        return service.buscar(OpcaoSelecionada.id(id, "id"));
    }

    @PostMapping
    @Operation(summary = "Cadastrar método de pagamento",
            description = "Cria um novo método de pagamento: preencha os campos abaixo (os marcados com * são obrigatórios) e clique em Execute. Devolve o registro criado com o seu id.")
    @ApiResponse(responseCode = "201", description = "Método de pagamento criado; o cabeçalho Location aponta para o novo recurso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos: a descrição é obrigatória e deve ter no máximo 100 caracteres",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    public ResponseEntity<MetodoPagamentoResponse> criar(@Valid @ParameterObject MetodoPagamentoRequest request) {
        MetodoPagamentoResponse criado = service.criar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{id}").buildAndExpand(criado.idMetodoPagamento()).toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar método de pagamento",
            description = "Altera o método de pagamento: informe o id e preencha os campos abaixo (os marcados com * são obrigatórios). Os dados atuais são substituídos pelos informados; campos opcionais deixados em branco são apagados.")
    @ApiResponse(responseCode = "200", description = "Método de pagamento atualizado")
    @ApiResponse(responseCode = "400", description = "Dados inválidos: a descrição é obrigatória e deve ter no máximo 100 caracteres",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "404", description = "O método de pagamento não existe",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    public MetodoPagamentoResponse atualizar(@Parameter(description = "Escolha na lista (método de pagamento)") @PathVariable String id,
                                  @Valid @ParameterObject MetodoPagamentoRequest request) {
        return service.atualizar(OpcaoSelecionada.id(id, "id"), request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir método de pagamento",
            description = "Remove definitivamente o método de pagamento. Só é permitido se nenhum outro cadastro estiver usando este registro.")
    @ApiResponse(responseCode = "204", description = "Método de pagamento excluído")
    @ApiResponse(responseCode = "404", description = "O método de pagamento não existe",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    @ApiResponse(responseCode = "409", description = "Não é possível excluir: o método de pagamento está em uso por outro cadastro",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResponse.class)))
    public void excluir(@Parameter(description = "Escolha na lista (método de pagamento)") @PathVariable String id) {
        service.excluir(OpcaoSelecionada.id(id, "id"));
    }
}
