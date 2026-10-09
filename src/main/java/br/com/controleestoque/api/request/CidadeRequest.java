package br.com.controleestoque.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CidadeRequest(
        @Schema(description = "Nome da cidade", example = "Campinas")
        @NotBlank(message = "O nome da cidade é obrigatório")
        @Size(max = 100, message = "O nome da cidade deve ter no máximo 100 caracteres")
        String nome,

        @Schema(description = "Escolha o estado na lista")
        @NotBlank(message = "O estado é obrigatório")
        String estado
) {
}
