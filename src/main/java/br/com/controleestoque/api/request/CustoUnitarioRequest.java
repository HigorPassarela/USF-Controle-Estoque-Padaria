package br.com.controleestoque.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CustoUnitarioRequest(
        @Schema(description = "Novo custo unitário de compra em reais (zero ou mais, até 2 casas). Use ponto como separador decimal", example = "5.20")
        @NotNull(message = "O custo unitário é obrigatório")
        @DecimalMin(value = "0.00", message = "O custo unitário não pode ser negativo")
        @Digits(integer = 8, fraction = 2, message = "O custo unitário deve ter no máximo 8 dígitos inteiros e 2 casas decimais")
        BigDecimal custoUnitario
) {
}
