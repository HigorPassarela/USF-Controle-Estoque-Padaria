package br.com.controleestoque.api.response;

import br.com.controleestoque.model.ProdutoFornecedor;

import java.math.BigDecimal;

public record ProdutoFornecedorResponse(
        Integer idProduto,
        Integer idFornecedor,
        String nomeFornecedor,
        String cnpjFornecedor,
        BigDecimal custoUnitario
) {
    public static ProdutoFornecedorResponse fromEntity(ProdutoFornecedor v) {
        return new ProdutoFornecedorResponse(
                v.getProduto().getIdProduto(),
                v.getFornecedor().getIdFornecedor(),
                v.getFornecedor().getNome(),
                v.getFornecedor().getCnpj(),
                v.getCustoUnitario()
        );
    }
}
