package br.com.controleestoque.api.response;

import br.com.controleestoque.model.CategoriaProduto;

public record CategoriaProdutoResponse(
        Integer idCategoriaProduto,
        String descricao
) {
    public static CategoriaProdutoResponse fromEntity(CategoriaProduto e) {
        return new CategoriaProdutoResponse(e.getIdCategoriaProduto(), e.getDescricao());
    }
}
