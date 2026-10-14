package br.com.controleestoque.api.response;

import br.com.controleestoque.model.MetodoPagamento;

public record MetodoPagamentoResponse(
        Integer idMetodoPagamento,
        String descricao
) {
    public static MetodoPagamentoResponse fromEntity(MetodoPagamento metodo) {
        return new MetodoPagamentoResponse(metodo.getIdMetodoPagamento(), metodo.getDescricao());
    }
}
