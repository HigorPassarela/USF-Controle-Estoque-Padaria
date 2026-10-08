package br.com.controleestoque.api;

import br.com.controleestoque.api.request.FornecedorRequest;
import br.com.controleestoque.api.response.FornecedorResponse;
import br.com.controleestoque.service.FornecedorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fornecedores")
public class FornecedorController {

    @Autowired
    private FornecedorService service;

    @GetMapping
    public List<FornecedorResponse> listar() {
        return service.listarTodos();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FornecedorResponse salvar(@Valid @RequestBody FornecedorRequest request) {
        return service.salvar(request);
    }
}
