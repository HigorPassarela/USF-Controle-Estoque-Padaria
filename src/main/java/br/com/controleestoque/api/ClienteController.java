package br.com.controleestoque.api;

import br.com.controleestoque.api.request.ClienteRequest;
import br.com.controleestoque.api.response.ClienteResponse;
import br.com.controleestoque.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    @Autowired
    private ClienteService service;

    @GetMapping
    public List<ClienteResponse> listar() {
        return service.listarTodos();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClienteResponse salvar(@Valid @RequestBody ClienteRequest request) {
        return service.salvar(request);
    }
}
