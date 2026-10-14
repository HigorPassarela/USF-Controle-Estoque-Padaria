package br.com.controleestoque.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record SaidaEstoqueRequest(
        @Schema(description = "Escolha o produto na lista (somente ativos)")
        @NotBlank(message = "O produto é obrigatório")
        String produto,

        @Schema(description = "Quantidade a baixar, maior que zero e com até 3 casas decimais. Use ponto como separador decimal", example = "1.5")
        @NotNull(message = "A quantidade é obrigatória")
        @Positive(message = "A quantidade deve ser maior que zero")
        @Digits(integer = 7, fraction = 3, message = "A quantidade deve ter no máximo 7 dígitos inteiros e 3 casas decimais")
        BigDecimal quantidade,

        @Schema(description = "Escolha o tipo de movimentação na lista (somente tipos de SAIDA)")
        @NotBlank(message = "O tipo de movimentação é obrigatório")
        String tipoMovimentacao,

        @Schema(description = "Observação sobre a saída (opcional)", example = "Fornada da manhã")
        @Size(max = 255, message = "A observação deve ter no máximo 255 caracteres")
        String observacao
) {
}
