package br.com.controleestoque.exception;

public class ValorInvalidoException extends RuntimeException {

    public ValorInvalidoException(String mensagem) {
        super(mensagem);
    }
}
