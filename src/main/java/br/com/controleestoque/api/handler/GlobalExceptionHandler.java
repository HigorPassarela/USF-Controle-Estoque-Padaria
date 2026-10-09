package br.com.controleestoque.api.handler;

import br.com.controleestoque.exception.RecursoNaoEncontradoException;
import br.com.controleestoque.exception.RegraNegocioException;
import br.com.controleestoque.exception.ValorInvalidoException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> naoEncontrado(RecursoNaoEncontradoException ex, HttpServletRequest request) {
        return resposta(HttpStatus.NOT_FOUND, "Não encontrado", ex.getMessage(), request);
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResponse> regraNegocio(RegraNegocioException ex, HttpServletRequest request) {
        return resposta(HttpStatus.CONFLICT, "Conflito", ex.getMessage(), request);
    }

    @ExceptionHandler(ValorInvalidoException.class)
    public ResponseEntity<ErroResponse> valorInvalido(ValorInvalidoException ex, HttpServletRequest request) {
        return resposta(HttpStatus.BAD_REQUEST, "Requisição inválida", ex.getMessage(), request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResponse> integridade(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.warn("Violação de integridade em {}", request.getRequestURI(), ex);
        return resposta(HttpStatus.CONFLICT, "Conflito",
                "A operação viola uma restrição de integridade dos dados", request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> validacao(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ErroResponse.CampoInvalido> campos = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> new ErroResponse.CampoInvalido(e.getField(),
                        e.isBindingFailure() ? "Valor inválido para este campo" : e.getDefaultMessage()))
                .toList();
        ErroResponse corpo = new ErroResponse(HttpStatus.BAD_REQUEST.value(), "Requisição inválida",
                "Um ou mais campos são inválidos", request.getRequestURI(), Instant.now(), campos);
        return ResponseEntity.badRequest().body(corpo);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErroResponse> requisicaoMalFormada(Exception ex, HttpServletRequest request) {
        return resposta(HttpStatus.BAD_REQUEST, "Requisição inválida",
                "Corpo ou parâmetro da requisição mal formado", request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErroResponse> metodoNaoPermitido(HttpRequestMethodNotSupportedException ex,
                                                           HttpServletRequest request) {
        return resposta(HttpStatus.METHOD_NOT_ALLOWED, "Método não permitido", ex.getMessage(), request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErroResponse> rotaInexistente(NoResourceFoundException ex, HttpServletRequest request) {
        return resposta(HttpStatus.NOT_FOUND, "Não encontrado", "Recurso não encontrado", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> inesperado(Exception ex, HttpServletRequest request) {
        log.error("Erro inesperado em {}", request.getRequestURI(), ex);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno",
                "Ocorreu um erro inesperado. Tente novamente mais tarde", request);
    }

    private ResponseEntity<ErroResponse> resposta(HttpStatus status, String erro, String mensagem,
                                                  HttpServletRequest request) {
        return ResponseEntity.status(status)
                .body(ErroResponse.of(status.value(), erro, mensagem, request.getRequestURI()));
    }
}
