package br.com.controleestoque.service;

import br.com.controleestoque.api.request.ProdutoRequest;
import br.com.controleestoque.api.response.ProdutoResponse;
import br.com.controleestoque.model.Produto;
import br.com.controleestoque.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProdutoService {

    @Autowired
    private ProdutoRepository produtoRepository;
    @Autowired
    private CategoriaProdutoRepository categoriaRepository;
    @Autowired
    private TipoProdutoRepository tipoProdutoRepository;
    @Autowired
    private UnidadeMedidaRepository unidadeMedidaRepository;
    @Autowired
    private FornecedorRepository fornecedorRepository;

    @Transactional(readOnly = true)
    public List<ProdutoResponse> listarTodos() {
        return produtoRepository.findAll()
                .stream()
                .map(ProdutoResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProdutoResponse salvar(ProdutoRequest request) {
        Produto produto = new Produto();
        produto.setNome(request.nome());
        produto.setPrecoVenda(request.precoVenda());
        produto.setQuantidadeEstoque(0);

        produto.setCategoriaProduto(categoriaRepository.findById(request.idCategoriaProduto())
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada")));

        produto.setTipoProduto(tipoProdutoRepository.findById(request.idTipoProduto())
                .orElseThrow(() -> new RuntimeException("Tipo de Produto não encontrado")));

        produto.setUnidadeMedida(unidadeMedidaRepository.findById(request.idUnidadeMedida())
                .orElseThrow(() -> new RuntimeException("Unidade de Medida não encontrada")));

        if (request.idFornecedor() != null) {
            produto.setFornecedor(fornecedorRepository.findById(request.idFornecedor())
                    .orElseThrow(() -> new RuntimeException("Fornecedor não encontrado")));
        }

        produto = produtoRepository.save(produto);
        return ProdutoResponse.fromEntity(produto);
    }
}
