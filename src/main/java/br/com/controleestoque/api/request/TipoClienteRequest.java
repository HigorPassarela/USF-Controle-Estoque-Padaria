package br.com.controleestoque.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TipoClienteRequest(
        @Schema(description = "Nome do tipo de cliente", example = "Pessoa física")
        @NotBlank(message = "A descrição do tipo de cliente é obrigatória")
        @Size(max = 100, message = "A descrição do tipo de cliente deve ter no máximo 100 caracteres")
        String descricao
) {
}
