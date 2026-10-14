package br.com.controleestoque.exception;

import lombok.Getter;

@Getter
public class ValorInvalidoException extends RuntimeException {

    /** Campo do request ao qual o erro se refere; nulo quando o erro não é de um campo específico. */
    private final String campo;

    public ValorInvalidoException(String mensagem) {
        this(null, mensagem);
    }

    public ValorInvalidoException(String campo, String mensagem) {
        super(mensagem);
        this.campo = campo;
    }
}
