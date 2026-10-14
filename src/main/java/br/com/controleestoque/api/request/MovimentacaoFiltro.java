package br.com.controleestoque.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record MovimentacaoFiltro(
        @Schema(description = "Escolha o produto na lista (opcional)")
        String produto,

        @Schema(description = "Escolha o lote na lista (opcional)")
        String lote,

        @Schema(description = "A partir desta data, inclusive, no formato AAAA-MM-DD (opcional)", example = "2026-10-01")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate de,

        @Schema(description = "Até esta data, inclusive, no formato AAAA-MM-DD (opcional)", example = "2026-10-31")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate ate
) {
}
