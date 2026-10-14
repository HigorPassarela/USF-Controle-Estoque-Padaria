package br.com.controleestoque.api.response;

import java.math.BigDecimal;
import java.util.List;

public record SaidaEstoqueResponse(
        Integer idProduto,
        BigDecimal quantidadeBaixada,
        List<MovimentacaoEstoqueResponse> movimentacoes,
        BigDecimal saldoProduto
) {
}
