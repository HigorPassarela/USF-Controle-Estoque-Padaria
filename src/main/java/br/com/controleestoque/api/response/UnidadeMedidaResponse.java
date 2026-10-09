package br.com.controleestoque.api.response;

import br.com.controleestoque.model.UnidadeMedida;

public record UnidadeMedidaResponse(
        Integer idUnidadeMedida,
        String descricao,
        String sigla
) {
    public static UnidadeMedidaResponse fromEntity(UnidadeMedida e) {
        return new UnidadeMedidaResponse(e.getIdUnidadeMedida(), e.getDescricao(), e.getSigla());
    }
}
