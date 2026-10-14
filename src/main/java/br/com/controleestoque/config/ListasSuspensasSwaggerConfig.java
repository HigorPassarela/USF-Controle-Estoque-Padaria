package br.com.controleestoque.config;

import br.com.controleestoque.model.Cidade;
import br.com.controleestoque.model.Lote;
import br.com.controleestoque.model.Bairro;
import br.com.controleestoque.repository.BairroRepository;
import br.com.controleestoque.repository.CategoriaProdutoRepository;
import br.com.controleestoque.repository.CidadeRepository;
import br.com.controleestoque.repository.ClienteRepository;
import br.com.controleestoque.repository.EstadoRepository;
import br.com.controleestoque.repository.FornecedorRepository;
import br.com.controleestoque.repository.LoteRepository;
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

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
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
    private final BairroRepository bairroRepository;
    private final ClienteRepository clienteRepository;
    private final FornecedorRepository fornecedorRepository;
    private final ProdutoRepository produtoRepository;
    private final LoteRepository loteRepository;

    @Bean
    public OpenApiCustomizer listasSuspensas() {
        // Parâmetro {id} de cada recurso -> registros existentes
        Map<String, Supplier<List<String>>> porRecurso = Map.ofEntries(
                Map.entry("metodos-pagamento", () -> opcoes(metodoPagamentoRepository.findAll(), m -> m.getIdMetodoPagamento(), m -> m.getDescricao())),
                Map.entry("tipos-produto", () -> opcoes(tipoProdutoRepository.findAll(), t -> t.getIdTipoProduto(), t -> t.getDescricao())),
                Map.entry("categorias-produto", () -> opcoes(categoriaProdutoRepository.findAll(), c -> c.getIdCategoriaProduto(), c -> c.getDescricao())),
                Map.entry("unidades-medida", () -> opcoes(unidadeMedidaRepository.findAll(), u -> u.getIdUnidadeMedida(), u -> u.getDescricao() + " (" + u.getSigla() + ")")),
                Map.entry("tipos-cliente", () -> opcoes(tipoClienteRepository.findAll(), t -> t.getIdTipoCliente(), t -> t.getDescricao())),
                Map.entry("tipos-movimentacao", () -> opcoes(tipoMovimentacaoRepository.findAll(), t -> t.getIdTipoMovimentacao(), t -> t.getDescricao() + " (" + t.getTipo() + ")")),
                Map.entry("tipos-movimentacao-entrada", () -> tiposMovimentacao("ENTRADA")),
                Map.entry("tipos-movimentacao-saida", () -> tiposMovimentacao("SAIDA")),
                Map.entry("estados", () -> opcoes(estadoRepository.findAll(), e -> e.getIdEstado(), e -> e.getNome() + " (" + e.getUf() + ")")),
                Map.entry("cidades", this::cidades),
                Map.entry("bairros", this::bairros),
                Map.entry("clientes", () -> opcoes(clienteRepository.findAllByAtivoTrue(), c -> c.getIdCliente(), c -> c.getNome())),
                Map.entry("fornecedores", () -> opcoes(fornecedorRepository.findAllByAtivoTrue(), f -> f.getIdFornecedor(), f -> f.getNome())),
                Map.entry("produtos", () -> opcoes(produtoRepository.findAllByAtivoTrue(), p -> p.getIdProduto(), p -> p.getNome())),
                Map.entry("lotes", this::lotes)
        );

        // Campo de escolha nos formulários (e filtros) -> registros que podem ser escolhidos
        Map<String, String> campoParaRecurso = Map.ofEntries(
                Map.entry("tipoCliente", "tipos-cliente"),
                Map.entry("cidade", "cidades"),
                Map.entry("bairro", "bairros"),
                Map.entry("categoria", "categorias-produto"),
                Map.entry("tipoProduto", "tipos-produto"),
                Map.entry("unidadeMedida", "unidades-medida"),
                Map.entry("fornecedor", "fornecedores"),
                Map.entry("estado", "estados"),
                Map.entry("produto", "produtos"),
                Map.entry("lote", "lotes"),
                Map.entry("tipoMovimentacao", "tipos-movimentacao")
        );

        // Parâmetros de caminho além do {id} -> registros existentes
        Map<String, String> parametroDeCaminhoParaRecurso = Map.of(
                "idFornecedor", "fornecedores",
                "idProduto", "produtos"
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
                        } else if ("path".equals(p.getIn())) {
                            origem = parametroDeCaminhoParaRecurso.get(p.getName());
                        } else if ("query".equals(p.getIn()) && "tipoMovimentacao".equals(p.getName())) {
                            // Entradas só aceitam tipos de ENTRADA; saídas, só de SAIDA
                            origem = caminho.endsWith("/entradas") ? "tipos-movimentacao-entrada"
                                    : caminho.endsWith("/saidas") ? "tipos-movimentacao-saida"
                                    : "tipos-movimentacao";
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
        // Segmento imediatamente antes do {id}: "/api/produtos/{id}" -> "produtos",
        // "/api/estoque/lotes/{id}/saidas" -> "lotes"
        String[] partes = caminho.split("/");
        for (int i = 1; i < partes.length; i++) {
            if ("{id}".equals(partes[i])) {
                return partes[i - 1];
            }
        }
        return partes.length > 2 ? partes[2] : "";
    }

    private List<String> tiposMovimentacao(String natureza) {
        return opcoes(tipoMovimentacaoRepository.findAll().stream().filter(t -> natureza.equals(t.getTipo())).toList(),
                t -> t.getIdTipoMovimentacao(), t -> t.getDescricao());
    }

    private List<String> lotes() {
        DateTimeFormatter data = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        List<Lote> comSaldo = loteRepository.findByQuantidadeAtualGreaterThan(BigDecimal.ZERO);
        return opcoes(comSaldo, Lote::getIdLote, l -> l.getProduto().getNome()
                + (l.getNumeroLote() != null ? " - lote " + l.getNumeroLote() : "")
                + (l.getDataValidade() != null ? ", vence " + l.getDataValidade().format(data) : ", sem validade")
                + ", saldo " + l.getQuantidadeAtual().stripTrailingZeros().toPlainString()
                + " " + l.getProduto().getUnidadeMedida().getSigla());
    }

    private List<String> cidades() {
        List<Cidade> todas = cidadeRepository.findAll();
        return opcoes(todas, Cidade::getIdCidade, c -> c.getNome() + " (" + c.getEstado().getUf() + ")");
    }

    private List<String> bairros() {
        List<Bairro> todos = bairroRepository.findAll();
        return opcoes(todos, Bairro::getIdBairro, b -> b.getNome() + " (" + b.getCidade().getNome() + "/" + b.getCidade().getEstado().getUf() + ")");
    }

    private static <T> List<String> opcoes(List<T> registros, Function<T, Integer> id, Function<T, String> nome) {
        return registros.stream()
                .sorted(Comparator.comparing(r -> nome.apply(r).toLowerCase()))
                .limit(LIMITE_OPCOES)
                .map(r -> id.apply(r) + " - " + nome.apply(r))
                .toList();
    }
}
