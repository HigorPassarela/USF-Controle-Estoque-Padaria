package br.com.controleestoque.api.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record FornecedorRequest(
        @NotBlank(message = "O nome do fornecedor é obrigatório")
        String nome,

        @NotBlank(message = "O CNPJ é obrigatório")
        String cnpj,

        String telefone,

        @Email(message = "Formato de e-mail inválido")
        String email,

        Integer idCidade
) {
}
