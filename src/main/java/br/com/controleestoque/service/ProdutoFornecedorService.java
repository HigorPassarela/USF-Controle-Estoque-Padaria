package br.com.controleestoque.service;

import br.com.controleestoque.api.request.CustoUnitarioRequest;
import br.com.controleestoque.api.request.ProdutoFornecedorRequest;
import br.com.controleestoque.api.response.ProdutoFornecedorResponse;
import br.com.controleestoque.exception.RecursoNaoEncontradoException;
import br.com.controleestoque.exception.RegraNegocioException;
import br.com.controleestoque.model.Fornecedor;
import br.com.controleestoque.model.Produto;
import br.com.controleestoque.model.ProdutoFornecedor;
import br.com.controleestoque.repository.FornecedorRepository;
import br.com.controleestoque.repository.ProdutoFornecedorRepository;
import br.com.controleestoque.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProdutoFornecedorService {

    private final ProdutoFornecedorRepository repository;
    private final ProdutoRepository produtoRepository;
    private final FornecedorRepository fornecedorRepository;

    @Transactional(readOnly = true)
    public List<ProdutoFornecedorResponse> listar(Integer idProduto) {
        buscarProdutoAtivo(idProduto);
        return repository.findByProduto_IdProdutoAndFornecedor_AtivoTrueOrderByFornecedor_NomeAsc(idProduto)
                .stream()
                .map(ProdutoFornecedorResponse::fromEntity)
                .toList();
    }

    @Transactional
    public ProdutoFornecedorResponse vincular(Integer idProduto, ProdutoFornecedorRequest request) {
        Produto produto = buscarProdutoAtivo(idProduto);
        Integer idFornecedor = OpcaoSelecionada.id(request.fornecedor(), "fornecedor");
        Fornecedor fornecedor = fornecedorRepository.findByIdFornecedorAndAtivoTrue(idFornecedor)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Fornecedor não encontrado"));
        if (repository.existsByProduto_IdProdutoAndFornecedor_IdFornecedor(idProduto, idFornecedor)) {
            throw new RegraNegocioException("Este fornecedor já está vinculado ao produto");
        }
        ProdutoFornecedor vinculo = new ProdutoFornecedor();
        vinculo.setProduto(produto);
        vinculo.setFornecedor(fornecedor);
        vinculo.setCustoUnitario(request.custoUnitario());
        return ProdutoFornecedorResponse.fromEntity(repository.save(vinculo));
    }

    @Transactional
    public ProdutoFornecedorResponse atualizarCusto(Integer idProduto, Integer idFornecedor, CustoUnitarioRequest request) {
        ProdutoFornecedor vinculo = buscarVinculo(idProduto, idFornecedor);
        vinculo.setCustoUnitario(request.custoUnitario());
        return ProdutoFornecedorResponse.fromEntity(repository.save(vinculo));
    }

    @Transactional
    public void desvincular(Integer idProduto, Integer idFornecedor) {
        // Lotes guardam fornecedor e custo próprios, então remover o vínculo não altera lotes já recebidos.
        repository.delete(buscarVinculo(idProduto, idFornecedor));
    }

    private ProdutoFornecedor buscarVinculo(Integer idProduto, Integer idFornecedor) {
        buscarProdutoAtivo(idProduto);
        return repository.findByProduto_IdProdutoAndFornecedor_IdFornecedor(idProduto, idFornecedor)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Este fornecedor não está vinculado ao produto"));
    }

    private Produto buscarProdutoAtivo(Integer idProduto) {
        return produtoRepository.findByIdProdutoAndAtivoTrue(idProduto)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado"));
    }
}
