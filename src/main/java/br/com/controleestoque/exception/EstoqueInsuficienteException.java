package br.com.controleestoque.exception;

public class EstoqueInsuficienteException extends RegraNegocioException {

    public EstoqueInsuficienteException(String mensagem) {
        super(mensagem);
    }
}
