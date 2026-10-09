package br.com.controleestoque.api.response;

import br.com.controleestoque.model.TipoCliente;

public record TipoClienteResponse(
        Integer idTipoCliente,
        String descricao
) {
    public static TipoClienteResponse fromEntity(TipoCliente e) {
        return new TipoClienteResponse(e.getIdTipoCliente(), e.getDescricao());
    }
}
