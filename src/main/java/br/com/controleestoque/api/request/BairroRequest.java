package br.com.controleestoque.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BairroRequest(
        @Schema(description = "Nome do bairro", example = "Cambuí")
        @NotBlank(message = "O nome do bairro é obrigatório")
        @Size(max = 100, message = "O nome do bairro deve ter no máximo 100 caracteres")
        String nome,

        @Schema(description = "Escolha a cidade na lista")
        @NotBlank(message = "A cidade é obrigatória")
        String cidade
) {
}
