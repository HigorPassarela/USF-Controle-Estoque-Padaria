package br.com.controleestoque.api.response;

import br.com.controleestoque.model.Bairro;

public record BairroResponse(
        Integer idBairro,
        String nome,
        Integer idCidade,
        String nomeCidade,
        String uf
) {
    public static BairroResponse fromEntity(Bairro b) {
        return new BairroResponse(
                b.getIdBairro(),
                b.getNome(),
                b.getCidade().getIdCidade(),
                b.getCidade().getNome(),
                b.getCidade().getEstado().getUf()
        );
    }
}
