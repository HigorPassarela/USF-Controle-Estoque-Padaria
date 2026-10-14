package br.com.controleestoque.api.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record LoteFiltro(
        @Schema(description = "Escolha o produto na lista (opcional)")
        String produto,

        @Schema(description = "Mostrar só lotes que ainda têm saldo (opcional)")
        Boolean somenteComSaldo,

        @Schema(description = "Mostrar só lotes vencidos (opcional)")
        Boolean somenteVencidos
) {
}
