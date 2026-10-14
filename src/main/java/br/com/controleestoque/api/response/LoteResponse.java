package br.com.controleestoque.api.response;

import br.com.controleestoque.model.Lote;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record LoteResponse(
        Integer idLote,
        Integer idProduto,
        String nomeProduto,
        String siglaUnidadeMedida,
        Integer idFornecedor,
        String nomeFornecedor,
        String numeroLote,
        Instant dataEntrada,
        LocalDate dataValidade,
        BigDecimal quantidadeInicial,
        BigDecimal quantidadeAtual,
        BigDecimal custoUnitario,
        boolean vencido
) {
    public static LoteResponse fromEntity(Lote l, LocalDate hoje) {
        return new LoteResponse(
                l.getIdLote(),
                l.getProduto().getIdProduto(),
                l.getProduto().getNome(),
                l.getProduto().getUnidadeMedida().getSigla(),
                l.getFornecedor() != null ? l.getFornecedor().getIdFornecedor() : null,
                l.getFornecedor() != null ? l.getFornecedor().getNome() : null,
                l.getNumeroLote(),
                l.getDataEntrada(),
                l.getDataValidade(),
                l.getQuantidadeInicial(),
                l.getQuantidadeAtual(),
                l.getCustoUnitario(),
                l.vencidoEm(hoje)
        );
    }
}
