package br.com.controleestoque.api.response;

import br.com.controleestoque.model.Produto;
import java.math.BigDecimal;

public record ProdutoResponse(
        Integer idProduto,
        String nome,
        BigDecimal precoVenda,
        Integer quantidadeEstoque,
        String nomeCategoria,
        String nomeUnidadeMedida
) {
    public static ProdutoResponse fromEntity(Produto produto) {
        return new ProdutoResponse(
                produto.getIdProduto(),
                produto.getNome(),
                produto.getPrecoVenda(),
                produto.getQuantidadeEstoque(),
                produto.getCategoriaProduto() != null ? produto.getCategoriaProduto().getDescricao() : null,
                produto.getUnidadeMedida() != null ? produto.getUnidadeMedida().getSigla() : null
        );
    }
}