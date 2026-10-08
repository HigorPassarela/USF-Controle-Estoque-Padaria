package br.com.controleestoque.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "cliente")
public class Cliente {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private Integer idCliente;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(name = "cpf_cnpj", nullable = false, length = 20)
    private String cpfCnpj;

    @Column(length = 20)
    private String telefone;

    @Column(length = 100)
    private String email;

    @ManyToOne
    @JoinColumn(name = "id_tipo_cliente", nullable = false)
    private TipoCliente tipoCliente;

    @ManyToOne
    @JoinColumn(name = "id_cidade")
    private Cidade cidade;
}