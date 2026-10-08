package br.com.controleestoque.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record ProdutoRequest(
        @NotBlank(message = "O nome do produto é obrigatório")
        String nome,

        @NotNull(message = "O preço de venda é obrigatório")
        @Positive(message = "O preço de venda deve ser maior que zero")
        BigDecimal precoVenda,

        @NotNull(message = "O ID da categoria é obrigatório")
        Integer idCategoriaProduto,

        @NotNull(message = "O ID do tipo de produto é obrigatório")
        Integer idTipoProduto,

        @NotNull(message = "O ID da unidade de medida é obrigatório")
        Integer idUnidadeMedida,

        Integer idFornecedor
) {
}