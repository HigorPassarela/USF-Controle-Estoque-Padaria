package br.com.controleestoque.api.response;

import java.math.BigDecimal;

public record EntradaEstoqueResponse(
        LoteResponse lote,
        MovimentacaoEstoqueResponse movimentacao,
        BigDecimal saldoProduto
) {
}
