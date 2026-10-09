package br.com.controleestoque.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record FornecedorRequest(
        @Schema(description = "Nome ou razão social do fornecedor", example = "Moinho Boa Farinha")
        @NotBlank(message = "O nome do fornecedor é obrigatório")
        String nome,

        @Schema(description = "CNPJ do fornecedor (único entre fornecedores ativos)", example = "12345678000199")
        @NotBlank(message = "O CNPJ é obrigatório")
        String cnpj,

        @Schema(description = "Telefone com DDD (opcional)", example = "1133334444")
        String telefone,

        @Schema(description = "E-mail do fornecedor (opcional)", example = "contato@fornecedor.com")
        @Email(message = "Formato de e-mail inválido")
        String email,

        @Schema(description = "Escolha a cidade na lista (opcional)")
        String cidade
) {
}
