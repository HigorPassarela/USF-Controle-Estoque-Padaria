package br.com.controleestoque.api.response;

import br.com.controleestoque.model.TipoMovimentacaoEstoque;

public record TipoMovimentacaoEstoqueResponse(
        Integer idTipoMovimentacao,
        String descricao,
        String tipo
) {
    public static TipoMovimentacaoEstoqueResponse fromEntity(TipoMovimentacaoEstoque e) {
        return new TipoMovimentacaoEstoqueResponse(e.getIdTipoMovimentacao(), e.getDescricao(), e.getTipo());
    }
}
