package br.com.controleestoque.api.response;

import br.com.controleestoque.model.Cliente;

public record ClienteResponse(
        Integer idCliente,
        String nome,
        String cpfCnpj,
        String telefone,
        String email,
        Integer idTipoCliente,
        String descricaoTipoCliente,
        Integer idCidade,
        String nomeCidade,
        Integer idBairro,
        String nomeBairro
) {
    public static ClienteResponse fromEntity(Cliente cliente) {
        return new ClienteResponse(
                cliente.getIdCliente(),
                cliente.getNome(),
                cliente.getCpfCnpj(),
                cliente.getTelefone(),
                cliente.getEmail(),
                cliente.getTipoCliente() != null ? cliente.getTipoCliente().getIdTipoCliente() : null,
                cliente.getTipoCliente() != null ? cliente.getTipoCliente().getDescricao() : null,
                cliente.getCidade() != null ? cliente.getCidade().getIdCidade() : null,
                cliente.getCidade() != null ? cliente.getCidade().getNome() : null,
                cliente.getBairro() != null ? cliente.getBairro().getIdBairro() : null,
                cliente.getBairro() != null ? cliente.getBairro().getNome() : null
        );
    }
}
