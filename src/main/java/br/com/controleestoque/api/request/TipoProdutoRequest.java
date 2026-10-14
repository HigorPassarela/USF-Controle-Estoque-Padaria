package br.com.controleestoque.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TipoProdutoRequest(
        @Schema(description = "Nome do tipo de produto", example = "Produto acabado")
        @NotBlank(message = "A descrição do tipo de produto é obrigatória")
        @Size(max = 100, message = "A descrição do tipo de produto deve ter no máximo 100 caracteres")
        String descricao
) {
}
