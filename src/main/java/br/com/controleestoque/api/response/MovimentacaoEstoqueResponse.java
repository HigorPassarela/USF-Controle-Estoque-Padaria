package br.com.controleestoque.api.response;

import br.com.controleestoque.model.MovimentacaoEstoque;

import java.math.BigDecimal;
import java.time.Instant;

public record MovimentacaoEstoqueResponse(
        Integer idMovimentacao,
        Integer idLote,
        String numeroLote,
        Integer idProduto,
        String nomeProduto,
        Integer idTipoMovimentacao,
        String descricaoTipoMovimentacao,
        String natureza,
        BigDecimal quantidade,
        Instant dataMovimentacao,
        String observacao
) {
    public static MovimentacaoEstoqueResponse fromEntity(MovimentacaoEstoque m) {
        return new MovimentacaoEstoqueResponse(
                m.getIdMovimentacao(),
                m.getLote().getIdLote(),
                m.getLote().getNumeroLote(),
                m.getProduto().getIdProduto(),
                m.getProduto().getNome(),
                m.getTipoMovimentacao().getIdTipoMovimentacao(),
                m.getTipoMovimentacao().getDescricao(),
                m.getTipoMovimentacao().getTipo(),
                m.getQuantidade(),
                m.getDataMovimentacao(),
                m.getObservacao()
        );
    }
}
