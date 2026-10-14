package br.com.controleestoque.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TipoMovimentacaoEstoqueRequest(
        @Schema(description = "Nome do tipo de movimentação", example = "Compra de fornecedor")
        @NotBlank(message = "A descrição do tipo de movimentação é obrigatória")
        @Size(max = 100, message = "A descrição do tipo de movimentação deve ter no máximo 100 caracteres")
        String descricao,

        @Schema(description = "Natureza da movimentação: ENTRADA (aumenta o estoque) ou SAIDA (diminui)",
                allowableValues = {"ENTRADA", "SAIDA"})
        @NotBlank(message = "O tipo (ENTRADA ou SAIDA) é obrigatório")
        @Pattern(regexp = "ENTRADA|SAIDA", message = "O tipo deve ser ENTRADA ou SAIDA")
        String tipo
) {
}
