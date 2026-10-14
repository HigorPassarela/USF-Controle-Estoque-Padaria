package br.com.controleestoque.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ProdutoRequest(
        @Schema(description = "Nome do produto", example = "Pão francês")
        @NotBlank(message = "O nome do produto é obrigatório")
        String nome,

        @Schema(description = "Preço de venda em reais, maior que zero. Use ponto como separador decimal", example = "0.75")
        @NotNull(message = "O preço de venda é obrigatório")
        @Positive(message = "O preço de venda deve ser maior que zero")
        BigDecimal precoVenda,

        @Schema(description = "Escolha a categoria na lista")
        @NotBlank(message = "A categoria é obrigatória")
        String categoria,

        @Schema(description = "Escolha o tipo de produto na lista")
        @NotBlank(message = "O tipo de produto é obrigatório")
        String tipoProduto,

        @Schema(description = "Escolha a unidade de medida na lista")
        @NotBlank(message = "A unidade de medida é obrigatória")
        String unidadeMedida,

        @Schema(description = "Escolha o fornecedor na lista (opcional)")
        String fornecedor
) {
}
