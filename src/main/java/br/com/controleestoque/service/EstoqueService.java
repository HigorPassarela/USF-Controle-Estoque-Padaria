package br.com.controleestoque.service;

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
import br.com.controleestoque.config.RelogioConfig;
import br.com.controleestoque.exception.EstoqueInsuficienteException;
import br.com.controleestoque.exception.RecursoNaoEncontradoException;
import br.com.controleestoque.exception.ValorInvalidoException;
import br.com.controleestoque.model.Fornecedor;
import br.com.controleestoque.model.Lote;
import br.com.controleestoque.model.MovimentacaoEstoque;
import br.com.controleestoque.model.Produto;
import br.com.controleestoque.model.ProdutoFornecedor;
import br.com.controleestoque.model.TipoMovimentacaoEstoque;
import br.com.controleestoque.repository.FornecedorRepository;
import br.com.controleestoque.repository.LoteRepository;
import br.com.controleestoque.repository.MovimentacaoEstoqueRepository;
import br.com.controleestoque.repository.ProdutoFornecedorRepository;
import br.com.controleestoque.repository.ProdutoRepository;
import br.com.controleestoque.repository.TipoMovimentacaoEstoqueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Único ponto que altera saldos: lotes (quantidade atual) e produto (quantidade em estoque) mudam
 * juntos, na mesma transação e com a linha do produto bloqueada, para que o saldo do produto seja
 * sempre a soma dos saldos dos lotes.
 */
@Service
@RequiredArgsConstructor
public class EstoqueService {

    static final String ENTRADA = "ENTRADA";
    static final String SAIDA = "SAIDA";

    private final LoteRepository loteRepository;
    private final MovimentacaoEstoqueRepository movimentacaoRepository;
    private final ProdutoRepository produtoRepository;
    private final FornecedorRepository fornecedorRepository;
    private final ProdutoFornecedorRepository produtoFornecedorRepository;
    private final TipoMovimentacaoEstoqueRepository tipoMovimentacaoRepository;
    private final Clock relogio;

    @Transactional
    public EntradaEstoqueResponse registrarEntrada(EntradaEstoqueRequest request) {
        LocalDate hoje = LocalDate.now(relogio);
        if (request.dataValidade() != null && request.dataValidade().isBefore(hoje)) {
            throw new ValorInvalidoException("dataValidade", "A data de validade não pode ser anterior a hoje");
        }
        TipoMovimentacaoEstoque tipo = tipoComNatureza(request.tipoMovimentacao(), ENTRADA);
        Produto produto = bloquearProdutoAtivo(OpcaoSelecionada.id(request.produto(), "produto"));

        Fornecedor fornecedor = null;
        BigDecimal custo = request.custoUnitario();
        Integer idFornecedor = OpcaoSelecionada.idOuNulo(request.fornecedor(), "fornecedor");
        if (idFornecedor != null) {
            fornecedor = fornecedorRepository.findByIdFornecedorAndAtivoTrue(idFornecedor)
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Fornecedor não encontrado"));
            if (custo == null) {
                custo = produtoFornecedorRepository
                        .findByProduto_IdProdutoAndFornecedor_IdFornecedor(produto.getIdProduto(), idFornecedor)
                        .map(ProdutoFornecedor::getCustoUnitario)
                        .orElse(null);
            }
        }

        Instant agora = Instant.now(relogio);
        Lote lote = new Lote();
        lote.setProduto(produto);
        lote.setFornecedor(fornecedor);
        lote.setNumeroLote(vazioComoNulo(request.numeroLote()));
        lote.setDataEntrada(agora);
        lote.setDataValidade(request.dataValidade());
        lote.setQuantidadeInicial(request.quantidade());
        lote.setQuantidadeAtual(request.quantidade());
        lote.setCustoUnitario(custo);
        loteRepository.save(lote);

        MovimentacaoEstoque movimentacao = registrarMovimentacao(lote, tipo, request.quantidade(), agora, request.observacao());
        produto.setQuantidadeEstoque(produto.getQuantidadeEstoque().add(request.quantidade()));

        return new EntradaEstoqueResponse(
                LoteResponse.fromEntity(lote, hoje),
                MovimentacaoEstoqueResponse.fromEntity(movimentacao),
                produto.getQuantidadeEstoque());
    }

    @Transactional
    public SaidaEstoqueResponse registrarSaida(SaidaEstoqueRequest request) {
        TipoMovimentacaoEstoque tipo = tipoComNatureza(request.tipoMovimentacao(), SAIDA);
        Produto produto = bloquearProdutoAtivo(OpcaoSelecionada.id(request.produto(), "produto"));
        LocalDate hoje = LocalDate.now(relogio);

        List<Lote> disponiveis = loteRepository.findDisponiveisFefo(produto.getIdProduto(), hoje);
        BigDecimal disponivel = disponiveis.stream().map(Lote::getQuantidadeAtual).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (disponivel.compareTo(request.quantidade()) < 0) {
            throw new EstoqueInsuficienteException("Estoque insuficiente de " + produto.getNome() + ": há "
                    + formatar(disponivel) + " " + produto.getUnidadeMedida().getSigla()
                    + " disponíveis (fora lotes vencidos) e foram pedidos " + formatar(request.quantidade()));
        }

        Instant agora = Instant.now(relogio);
        BigDecimal falta = request.quantidade();
        List<MovimentacaoEstoqueResponse> movimentacoes = new ArrayList<>();
        for (Lote lote : disponiveis) {
            if (falta.signum() == 0) {
                break;
            }
            BigDecimal baixa = lote.getQuantidadeAtual().min(falta);
            lote.setQuantidadeAtual(lote.getQuantidadeAtual().subtract(baixa));
            movimentacoes.add(MovimentacaoEstoqueResponse.fromEntity(
                    registrarMovimentacao(lote, tipo, baixa, agora, request.observacao())));
            falta = falta.subtract(baixa);
        }
        produto.setQuantidadeEstoque(produto.getQuantidadeEstoque().subtract(request.quantidade()));

        return new SaidaEstoqueResponse(produto.getIdProduto(), request.quantidade(), movimentacoes,
                produto.getQuantidadeEstoque());
    }

    @Transactional
    public SaidaEstoqueResponse registrarSaidaDoLote(Integer idLote, SaidaLoteRequest request) {
        TipoMovimentacaoEstoque tipo = tipoComNatureza(request.tipoMovimentacao(), SAIDA);
        Integer idProduto = loteRepository.findIdProdutoDoLote(idLote)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Lote não encontrado"));
        // Bloqueia o produto antes de ler o lote, para ler o saldo já protegido de outras operações.
        Produto produto = produtoRepository.findParaMovimentarEstoque(idProduto)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado"));
        Lote lote = loteRepository.findById(idLote)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Lote não encontrado"));

        if (lote.getQuantidadeAtual().compareTo(request.quantidade()) < 0) {
            throw new EstoqueInsuficienteException("Saldo insuficiente no lote " + idLote + ": há "
                    + formatar(lote.getQuantidadeAtual()) + " " + produto.getUnidadeMedida().getSigla()
                    + " e foram pedidos " + formatar(request.quantidade()));
        }

        lote.setQuantidadeAtual(lote.getQuantidadeAtual().subtract(request.quantidade()));
        MovimentacaoEstoque movimentacao = registrarMovimentacao(lote, tipo, request.quantidade(),
                Instant.now(relogio), request.observacao());
        produto.setQuantidadeEstoque(produto.getQuantidadeEstoque().subtract(request.quantidade()));

        return new SaidaEstoqueResponse(produto.getIdProduto(), request.quantidade(),
                List.of(MovimentacaoEstoqueResponse.fromEntity(movimentacao)), produto.getQuantidadeEstoque());
    }

    @Transactional(readOnly = true)
    public List<LoteResponse> listarLotes(LoteFiltro filtro) {
        LocalDate hoje = LocalDate.now(relogio);
        Integer idProduto = OpcaoSelecionada.idOuNulo(filtro.produto(), "produto");
        Specification<Lote> spec = (root, query, cb) -> cb.conjunction();
        if (idProduto != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("produto").get("idProduto"), idProduto));
        }
        if (Boolean.TRUE.equals(filtro.somenteComSaldo())) {
            spec = spec.and((root, query, cb) -> cb.greaterThan(root.get("quantidadeAtual"), BigDecimal.ZERO));
        }
        if (Boolean.TRUE.equals(filtro.somenteVencidos())) {
            spec = spec.and((root, query, cb) -> cb.lessThan(root.get("dataValidade"), hoje));
        }
        // Criteria não aceita NULLS LAST: ordena por id no banco e por validade aqui (sem validade por último).
        return loteRepository.findAll(spec, Sort.by("idLote")).stream()
                .sorted(Comparator.comparing(Lote::getDataValidade, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(l -> LoteResponse.fromEntity(l, hoje))
                .toList();
    }

    @Transactional(readOnly = true)
    public LoteResponse buscarLote(Integer idLote) {
        return loteRepository.findById(idLote)
                .map(l -> LoteResponse.fromEntity(l, LocalDate.now(relogio)))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Lote não encontrado"));
    }

    @Transactional(readOnly = true)
    public List<MovimentacaoEstoqueResponse> listarMovimentacoes(MovimentacaoFiltro filtro) {
        Integer idProduto = OpcaoSelecionada.idOuNulo(filtro.produto(), "produto");
        Integer idLote = OpcaoSelecionada.idOuNulo(filtro.lote(), "lote");
        if (filtro.de() != null && filtro.ate() != null && filtro.ate().isBefore(filtro.de())) {
            throw new ValorInvalidoException("ate", "A data final não pode ser anterior à data inicial");
        }
        Specification<MovimentacaoEstoque> spec = (root, query, cb) -> cb.conjunction();
        if (idProduto != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("produto").get("idProduto"), idProduto));
        }
        if (idLote != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("lote").get("idLote"), idLote));
        }
        if (filtro.de() != null) {
            Instant inicio = filtro.de().atStartOfDay(RelogioConfig.FUSO_PADARIA).toInstant();
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("dataMovimentacao"), inicio));
        }
        if (filtro.ate() != null) {
            Instant fim = filtro.ate().plusDays(1).atStartOfDay(RelogioConfig.FUSO_PADARIA).toInstant();
            spec = spec.and((root, query, cb) -> cb.lessThan(root.get("dataMovimentacao"), fim));
        }
        Sort ordem = Sort.by(Sort.Order.desc("dataMovimentacao"), Sort.Order.desc("idMovimentacao"));
        return movimentacaoRepository.findAll(spec, ordem).stream()
                .map(MovimentacaoEstoqueResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public MovimentacaoEstoqueResponse buscarMovimentacao(Integer idMovimentacao) {
        return movimentacaoRepository.findById(idMovimentacao)
                .map(MovimentacaoEstoqueResponse::fromEntity)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Movimentação não encontrada"));
    }

    @Transactional(readOnly = true)
    public List<SaldoProdutoResponse> listarSaldos() {
        LocalDate hoje = LocalDate.now(relogio);
        Map<Integer, List<Lote>> lotesPorProduto = loteRepository
                .findByQuantidadeAtualGreaterThanAndProduto_AtivoTrue(BigDecimal.ZERO).stream()
                .collect(Collectors.groupingBy(l -> l.getProduto().getIdProduto()));
        return produtoRepository.findAllByAtivoTrue().stream()
                .sorted(Comparator.comparing(p -> p.getNome().toLowerCase()))
                .map(p -> saldo(p, lotesPorProduto.getOrDefault(p.getIdProduto(), List.of()), hoje))
                .toList();
    }

    @Transactional(readOnly = true)
    public SaldoProdutoResponse buscarSaldo(Integer idProduto) {
        Produto produto = produtoRepository.findByIdProdutoAndAtivoTrue(idProduto)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado"));
        List<Lote> lotes = loteRepository.findByProduto_IdProdutoAndQuantidadeAtualGreaterThan(idProduto, BigDecimal.ZERO);
        return saldo(produto, lotes, LocalDate.now(relogio));
    }

    private SaldoProdutoResponse saldo(Produto produto, List<Lote> lotesComSaldo, LocalDate hoje) {
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal vencido = BigDecimal.ZERO;
        LocalDate proximoVencimento = null;
        for (Lote lote : lotesComSaldo) {
            total = total.add(lote.getQuantidadeAtual());
            if (lote.vencidoEm(hoje)) {
                vencido = vencido.add(lote.getQuantidadeAtual());
            } else if (lote.getDataValidade() != null
                    && (proximoVencimento == null || lote.getDataValidade().isBefore(proximoVencimento))) {
                proximoVencimento = lote.getDataValidade();
            }
        }
        return new SaldoProdutoResponse(produto.getIdProduto(), produto.getNome(),
                produto.getUnidadeMedida().getSigla(), total, total.subtract(vencido), vencido, proximoVencimento);
    }

    private Produto bloquearProdutoAtivo(Integer idProduto) {
        return produtoRepository.findParaMovimentarEstoque(idProduto)
                .filter(p -> Boolean.TRUE.equals(p.getAtivo()))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado"));
    }

    private TipoMovimentacaoEstoque tipoComNatureza(String valor, String natureza) {
        TipoMovimentacaoEstoque tipo = tipoMovimentacaoRepository.findById(OpcaoSelecionada.id(valor, "tipoMovimentacao"))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Tipo de movimentação de estoque não encontrado"));
        if (!Objects.equals(tipo.getTipo(), natureza)) {
            throw new ValorInvalidoException("tipoMovimentacao", ENTRADA.equals(natureza)
                    ? "O tipo de movimentação deve ser de entrada (ENTRADA)"
                    : "O tipo de movimentação deve ser de saída (SAIDA)");
        }
        return tipo;
    }

    private MovimentacaoEstoque registrarMovimentacao(Lote lote, TipoMovimentacaoEstoque tipo, BigDecimal quantidade,
                                                      Instant quando, String observacao) {
        MovimentacaoEstoque m = new MovimentacaoEstoque();
        m.setLote(lote);
        m.setProduto(lote.getProduto());
        m.setTipoMovimentacao(tipo);
        m.setQuantidade(quantidade);
        m.setDataMovimentacao(quando);
        m.setObservacao(vazioComoNulo(observacao));
        return movimentacaoRepository.save(m);
    }

    private static String vazioComoNulo(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private static String formatar(BigDecimal quantidade) {
        return quantidade.stripTrailingZeros().toPlainString();
    }
}
