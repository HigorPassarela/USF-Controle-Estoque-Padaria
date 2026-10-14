package br.com.controleestoque.api.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SaldoProdutoResponse(
        Integer idProduto,
        String nomeProduto,
        String siglaUnidadeMedida,
        BigDecimal saldoTotal,
        BigDecimal saldoDisponivel,
        BigDecimal saldoVencido,
        LocalDate proximoVencimento
) {
}
