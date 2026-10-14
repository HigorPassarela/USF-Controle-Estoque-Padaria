package br.com.controleestoque.service;

import br.com.controleestoque.api.response.EstadoResponse;
import br.com.controleestoque.exception.RecursoNaoEncontradoException;
import br.com.controleestoque.repository.EstadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EstadoService {

    private final EstadoRepository repository;

    @Transactional(readOnly = true)
    public List<EstadoResponse> listarTodos() {
        return repository.findAll(Sort.by("nome")).stream().map(EstadoResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public EstadoResponse buscar(Integer id) {
        return repository.findById(id)
                .map(EstadoResponse::fromEntity)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Estado não encontrado"));
    }
}
