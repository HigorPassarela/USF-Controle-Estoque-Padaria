package br.com.controleestoque.api.response;

import br.com.controleestoque.model.TipoProduto;

public record TipoProdutoResponse(
        Integer idTipoProduto,
        String descricao
) {
    public static TipoProdutoResponse fromEntity(TipoProduto e) {
        return new TipoProdutoResponse(e.getIdTipoProduto(), e.getDescricao());
    }
}
