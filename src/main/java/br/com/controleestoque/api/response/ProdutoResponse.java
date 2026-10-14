package br.com.controleestoque.api.response;

import br.com.controleestoque.model.Produto;

import java.math.BigDecimal;

public record ProdutoResponse(
        Integer idProduto,
        String nome,
        BigDecimal precoVenda,
        BigDecimal quantidadeEstoque,
        Integer idCategoriaProduto,
        String nomeCategoria,
        Integer idTipoProduto,
        String descricaoTipoProduto,
        Integer idUnidadeMedida,
        String nomeUnidadeMedida,
        Integer idFornecedor,
        String nomeFornecedor
) {
    public static ProdutoResponse fromEntity(Produto produto) {
        return new ProdutoResponse(
                produto.getIdProduto(),
                produto.getNome(),
                produto.getPrecoVenda(),
                produto.getQuantidadeEstoque(),
                produto.getCategoriaProduto() != null ? produto.getCategoriaProduto().getIdCategoriaProduto() : null,
                produto.getCategoriaProduto() != null ? produto.getCategoriaProduto().getDescricao() : null,
                produto.getTipoProduto() != null ? produto.getTipoProduto().getIdTipoProduto() : null,
                produto.getTipoProduto() != null ? produto.getTipoProduto().getDescricao() : null,
                produto.getUnidadeMedida() != null ? produto.getUnidadeMedida().getIdUnidadeMedida() : null,
                produto.getUnidadeMedida() != null ? produto.getUnidadeMedida().getSigla() : null,
                produto.getFornecedor() != null ? produto.getFornecedor().getIdFornecedor() : null,
                produto.getFornecedor() != null ? produto.getFornecedor().getNome() : null
        );
    }
}
