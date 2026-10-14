package br.com.controleestoque.api.response;

import br.com.controleestoque.model.Fornecedor;

public record FornecedorResponse(
        Integer idFornecedor,
        String nome,
        String cnpj,
        String telefone,
        String email,
        Integer idCidade,
        String nomeCidade,
        Integer idBairro,
        String nomeBairro
) {
    public static FornecedorResponse fromEntity(Fornecedor fornecedor) {
        return new FornecedorResponse(
                fornecedor.getIdFornecedor(),
                fornecedor.getNome(),
                fornecedor.getCnpj(),
                fornecedor.getTelefone(),
                fornecedor.getEmail(),
                fornecedor.getCidade() != null ? fornecedor.getCidade().getIdCidade() : null,
                fornecedor.getCidade() != null ? fornecedor.getCidade().getNome() : null,
                fornecedor.getBairro() != null ? fornecedor.getBairro().getIdBairro() : null,
                fornecedor.getBairro() != null ? fornecedor.getBairro().getNome() : null
        );
    }
}
