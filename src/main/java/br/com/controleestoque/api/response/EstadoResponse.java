package br.com.controleestoque.api.response;

import br.com.controleestoque.model.Estado;

public record EstadoResponse(
        Integer idEstado,
        String nome,
        String uf
) {
    public static EstadoResponse fromEntity(Estado e) {
        return new EstadoResponse(e.getIdEstado(), e.getNome(), e.getUf());
    }
}
