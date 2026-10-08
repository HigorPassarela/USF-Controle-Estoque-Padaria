package br.com.controleestoque.api.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ClienteRequest(
        @NotBlank(message = "O nome do cliente é obrigatório")
        String nome,

        @NotBlank(message = "O CPF/CNPJ é obrigatório")
        String cpfCnpj,

        String telefone,

        @Email(message = "Formato de e-mail inválido")
        String email,

        @NotNull(message = "O ID do tipo de cliente é obrigatório")
        Integer idTipoCliente,

        Integer idCidade
) {
}