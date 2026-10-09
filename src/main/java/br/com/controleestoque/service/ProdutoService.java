package br.com.controleestoque.service;

import br.com.controleestoque.api.request.ProdutoRequest;
import br.com.controleestoque.api.response.ProdutoResponse;
import br.com.controleestoque.exception.RecursoNaoEncontradoException;
import br.com.controleestoque.model.Produto;
import br.com.controleestoque.repository.CategoriaProdutoRepository;
import br.com.controleestoque.repository.FornecedorRepository;
import br.com.controleestoque.repository.ProdutoRepository;
import br.com.controleestoque.repository.TipoProdutoRepository;
import br.com.controleestoque.repository.UnidadeMedidaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final CategoriaProdutoRepository categoriaRepository;
    private final TipoProdutoRepository tipoProdutoRepository;
    private final UnidadeMedidaRepository unidadeMedidaRepository;
    private final FornecedorRepository fornecedorRepository;

    @Transactional(readOnly = true)
    public List<ProdutoResponse> listarTodos() {
        return produtoRepository.findAllByAtivoTrue()
                .stream()
                .map(ProdutoResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProdutoResponse buscar(Integer id) {
        return ProdutoResponse.fromEntity(buscarAtivo(id));
    }

    @Transactional
    public ProdutoResponse criar(ProdutoRequest request) {
        Produto produto = new Produto();
        produto.setQuantidadeEstoque(0);
        preencher(produto, request);
        return ProdutoResponse.fromEntity(produtoRepository.save(produto));
    }

    @Transactional
    public ProdutoResponse atualizar(Integer id, ProdutoRequest request) {
        // O saldo (quantidadeEstoque) não é alterado aqui: só o serviço de estoque o modifica.
        Produto produto = buscarAtivo(id);
        preencher(produto, request);
        return ProdutoResponse.fromEntity(produtoRepository.save(produto));
    }

    @Transactional
    public void excluir(Integer id) {
        Produto produto = buscarAtivo(id);
        produto.setAtivo(false);
        produtoRepository.save(produto);
    }

    private Produto buscarAtivo(Integer id) {
        return produtoRepository.findByIdProdutoAndAtivoTrue(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado"));
    }

    private void preencher(Produto produto, ProdutoRequest request) {
        produto.setNome(request.nome());
        produto.setPrecoVenda(request.precoVenda());

        produto.setCategoriaProduto(categoriaRepository.findById(OpcaoSelecionada.id(request.categoria(), "categoria"))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria não encontrada")));

        produto.setTipoProduto(tipoProdutoRepository.findById(OpcaoSelecionada.id(request.tipoProduto(), "tipoProduto"))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Tipo de Produto não encontrado")));

        produto.setUnidadeMedida(unidadeMedidaRepository.findById(OpcaoSelecionada.id(request.unidadeMedida(), "unidadeMedida"))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Unidade de Medida não encontrada")));

        Integer idFornecedor = OpcaoSelecionada.idOuNulo(request.fornecedor(), "fornecedor");
        produto.setFornecedor(idFornecedor == null ? null
                : fornecedorRepository.findByIdFornecedorAndAtivoTrue(idFornecedor)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Fornecedor não encontrado")));
    }
}
