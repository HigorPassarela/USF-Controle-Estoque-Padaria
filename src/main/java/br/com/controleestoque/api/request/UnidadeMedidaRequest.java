package br.com.controleestoque.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UnidadeMedidaRequest(
        @Schema(description = "Nome da unidade de medida", example = "Quilograma")
        @NotBlank(message = "A descrição da unidade de medida é obrigatória")
        @Size(max = 100, message = "A descrição da unidade de medida deve ter no máximo 100 caracteres")
        String descricao,

        @Schema(description = "Sigla da unidade (até 10 caracteres)", example = "kg")
        @NotBlank(message = "A sigla da unidade de medida é obrigatória")
        @Size(max = 10, message = "A sigla da unidade de medida deve ter no máximo 10 caracteres")
        String sigla
) {
}
