package br.com.controleestoque.api.response;

import br.com.controleestoque.model.Fornecedor;

public record FornecedorResponse(
        Integer idFornecedor,
        String nome,
        String cnpj,
        String telefone,
        String email,
        String nomeCidade
) {
    public static FornecedorResponse fromEntity(Fornecedor fornecedor) {
        return new FornecedorResponse(
                fornecedor.getIdFornecedor(),
                fornecedor.getNome(),
                fornecedor.getCnpj(),
                fornecedor.getTelefone(),
                fornecedor.getEmail(),
                fornecedor.getCidade() != null ? fornecedor.getCidade().getNome() : null
        );
    }
}
