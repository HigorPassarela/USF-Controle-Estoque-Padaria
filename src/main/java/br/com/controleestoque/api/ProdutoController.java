package br.com.controleestoque.api;

import br.com.controleestoque.api.request.ProdutoRequest;
import br.com.controleestoque.api.response.ProdutoResponse;
import br.com.controleestoque.service.ProdutoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {

    @Autowired
    private ProdutoService service;

    @GetMapping
    public List<ProdutoResponse> listar() {
        return service.listarTodos();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProdutoResponse salvar(@Valid @RequestBody ProdutoRequest request) {
        return service.salvar(request);
    }
}
