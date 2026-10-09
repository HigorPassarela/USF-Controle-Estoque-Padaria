package br.com.controleestoque.config;

import br.com.controleestoque.model.Cidade;
import br.com.controleestoque.repository.CategoriaProdutoRepository;
import br.com.controleestoque.repository.CidadeRepository;
import br.com.controleestoque.repository.ClienteRepository;
import br.com.controleestoque.repository.EstadoRepository;
import br.com.controleestoque.repository.FornecedorRepository;
import br.com.controleestoque.repository.MetodoPagamentoRepository;
import br.com.controleestoque.repository.ProdutoRepository;
import br.com.controleestoque.repository.TipoClienteRepository;
import br.com.controleestoque.repository.TipoMovimentacaoEstoqueRepository;
import br.com.controleestoque.repository.TipoProdutoRepository;
import br.com.controleestoque.repository.UnidadeMedidaRepository;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.parameters.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Transforma os campos de escolha do Swagger em listas suspensas preenchidas com o que está
 * cadastrado no banco. Cada opção tem o formato "id - nome" (ex.: "3 - Pães"): o usuário vê o
 * nome e a API lê o número do início (ver OpcaoSelecionada).
 *
 * <p>As listas são montadas a cada carga da documentação (springdoc.cache.disabled=true), então
 * um registro novo aparece ao recarregar a página do Swagger.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class ListasSuspensasSwaggerConfig {

    private static final int LIMITE_OPCOES = 1000;

    private final MetodoPagamentoRepository metodoPagamentoRepository;
    private final TipoProdutoRepository tipoProdutoRepository;
    private final CategoriaProdutoRepository categoriaProdutoRepository;
    private final UnidadeMedidaRepository unidadeMedidaRepository;
    private final TipoClienteRepository tipoClienteRepository;
    private final TipoMovimentacaoEstoqueRepository tipoMovimentacaoRepository;
    private final EstadoRepository estadoRepository;
    private final CidadeRepository cidadeRepository;
    private final ClienteRepository clienteRepository;
    private final FornecedorRepository fornecedorRepository;
    private final ProdutoRepository produtoRepository;

    @Bean
    public OpenApiCustomizer listasSuspensas() {
        // Parâmetro {id} de cada recurso -> registros existentes
        Map<String, Supplier<List<String>>> porRecurso = Map.ofEntries(
                Map.entry("metodos-pagamento", () -> opcoes(metodoPagamentoRepository.findAll(), m -> m.getIdMetodoPagamento(), m -> m.getDescricao())),
                Map.entry("tipos-produto", () -> opcoes(tipoProdutoRepository.findAll(), t -> t.getIdTipoProduto(), t -> t.getDescricao())),
                Map.entry("categorias-produto", () -> opcoes(categoriaProdutoRepository.findAll(), c -> c.getIdCategoriaProduto(), c -> c.getDescricao())),
                Map.entry("unidades-medida", () -> opcoes(unidadeMedidaRepository.findAll(), u -> u.getIdUnidadeMedida(), u -> u.getDescricao() + " (" + u.getSigla() + ")")),
                Map.entry("tipos-cliente", () -> opcoes(tipoClienteRepository.findAll(), t -> t.getIdTipoCliente(), t -> t.getDescricao())),
                Map.entry("tipos-movimentacao", () -> opcoes(tipoMovimentacaoRepository.findAll(), t -> t.getIdTipoMovimentacao(), t -> t.getDescricao())),
                Map.entry("estados", () -> opcoes(estadoRepository.findAll(), e -> e.getIdEstado(), e -> e.getNome() + " (" + e.getUf() + ")")),
                Map.entry("cidades", this::cidades),
                Map.entry("clientes", () -> opcoes(clienteRepository.findAllByAtivoTrue(), c -> c.getIdCliente(), c -> c.getNome())),
                Map.entry("fornecedores", () -> opcoes(fornecedorRepository.findAllByAtivoTrue(), f -> f.getIdFornecedor(), f -> f.getNome())),
                Map.entry("produtos", () -> opcoes(produtoRepository.findAllByAtivoTrue(), p -> p.getIdProduto(), p -> p.getNome()))
        );

        // Campo de escolha nos formulários de cadastro -> registros que podem ser escolhidos
        Map<String, String> campoParaRecurso = Map.of(
                "tipoCliente", "tipos-cliente",
                "cidade", "cidades",
                "categoria", "categorias-produto",
                "tipoProduto", "tipos-produto",
                "unidadeMedida", "unidades-medida",
                "fornecedor", "fornecedores",
                "estado", "estados"
        );

        return openApi -> {
            if (openApi.getPaths() == null) {
                return;
            }
            Map<String, List<String>> cache = new java.util.HashMap<>();
            Function<String, List<String>> carregar = recurso ->
                    cache.computeIfAbsent(recurso, r -> {
                        try {
                            return porRecurso.get(r).get();
                        } catch (RuntimeException e) {
                            log.warn("Não foi possível montar a lista suspensa de '{}'", r, e);
                            return List.of();
                        }
                    });

            openApi.getPaths().forEach((caminho, item) -> {
                String recurso = recursoDoCaminho(caminho);
                for (PathItem.HttpMethod metodo : item.readOperationsMap().keySet()) {
                    List<Parameter> parametros = item.readOperationsMap().get(metodo).getParameters();
                    if (parametros == null) {
                        continue;
                    }
                    for (Parameter p : parametros) {
                        String origem = null;
                        if ("path".equals(p.getIn()) && "id".equals(p.getName())) {
                            origem = recurso;
                        } else if ("query".equals(p.getIn()) && campoParaRecurso.containsKey(p.getName())) {
                            origem = campoParaRecurso.get(p.getName());
                        }
                        if (origem != null && porRecurso.containsKey(origem) && p.getSchema() != null) {
                            List<String> lista = carregar.apply(origem);
                            if (!lista.isEmpty()) {
                                p.getSchema().setEnum(new java.util.ArrayList<Object>(lista));
                            }
                        }
                    }
                }
            });
        };
    }

    private static String recursoDoCaminho(String caminho) {
        // "/api/produtos/{id}" -> "produtos"
        String[] partes = caminho.split("/");
        return partes.length > 2 ? partes[2] : "";
    }

    private List<String> cidades() {
        List<Cidade> todas = cidadeRepository.findAll();
        return opcoes(todas, Cidade::getIdCidade, c -> c.getNome() + " (" + c.getEstado().getUf() + ")");
    }

    private static <T> List<String> opcoes(List<T> registros, Function<T, Integer> id, Function<T, String> nome) {
        return registros.stream()
                .sorted(Comparator.comparing(r -> nome.apply(r).toLowerCase()))
                .limit(LIMITE_OPCOES)
                .map(r -> id.apply(r) + " - " + nome.apply(r))
                .toList();
    }
}
