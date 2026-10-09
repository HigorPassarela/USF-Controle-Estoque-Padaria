package br.com.controleestoque.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MetodoPagamentoRequest(
        @Schema(description = "Nome da forma de pagamento", example = "Pix")
        @NotBlank(message = "A descrição do método de pagamento é obrigatória")
        @Size(max = 100, message = "A descrição deve ter no máximo 100 caracteres")
        String descricao
) {
}
