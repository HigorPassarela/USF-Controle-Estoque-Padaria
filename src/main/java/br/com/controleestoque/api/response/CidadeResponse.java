package br.com.controleestoque.api.response;

import br.com.controleestoque.model.Cidade;

public record CidadeResponse(
        Integer idCidade,
        String nome,
        Integer idEstado,
        String nomeEstado,
        String uf
) {
    public static CidadeResponse fromEntity(Cidade c) {
        return new CidadeResponse(
                c.getIdCidade(),
                c.getNome(),
                c.getEstado().getIdEstado(),
                c.getEstado().getNome(),
                c.getEstado().getUf()
        );
    }
}
