package br.com.controleestoque.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaProdutoRequest(
        @Schema(description = "Nome da categoria", example = "Pães")
        @NotBlank(message = "A descrição da categoria é obrigatória")
        @Size(max = 100, message = "A descrição da categoria deve ter no máximo 100 caracteres")
        String descricao
) {
}
