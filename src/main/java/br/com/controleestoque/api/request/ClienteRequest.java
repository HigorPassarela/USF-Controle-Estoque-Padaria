package br.com.controleestoque.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ClienteRequest(
        @Schema(description = "Nome completo do cliente", example = "Maria da Silva")
        @NotBlank(message = "O nome do cliente é obrigatório")
        String nome,

        @Schema(description = "CPF ou CNPJ do cliente (único entre clientes ativos)", example = "12345678901")
        @NotBlank(message = "O CPF/CNPJ é obrigatório")
        String cpfCnpj,

        @Schema(description = "Telefone com DDD (opcional)", example = "11999998888")
        String telefone,

        @Schema(description = "E-mail do cliente (opcional)", example = "maria@email.com")
        @Email(message = "Formato de e-mail inválido")
        String email,

        @Schema(description = "Escolha o tipo de cliente na lista")
        @NotBlank(message = "O tipo de cliente é obrigatório")
        String tipoCliente,

        @Schema(description = "Escolha a cidade na lista (opcional)")
        String cidade
) {
}
