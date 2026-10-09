package br.com.controleestoque.service;

import br.com.controleestoque.exception.ValorInvalidoException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lê o id de uma opção escolhida em lista suspensa no Swagger.
 * As opções têm o formato "3 - Pães"; também é aceito apenas o número ("3").
 */
public final class OpcaoSelecionada {

    private static final Pattern INICIO_NUMERICO = Pattern.compile("^\\s*(\\d+)(?:\\s*-.*)?$");

    private OpcaoSelecionada() {
    }

    /** Devolve o id da opção, ou null se o valor estiver vazio (campo opcional não preenchido). */
    public static Integer idOuNulo(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        Matcher m = INICIO_NUMERICO.matcher(valor);
        if (!m.matches()) {
            throw new ValorInvalidoException("Opção inválida para o campo '" + campo
                    + "'. Escolha um item da lista (ex.: \"1 - Nome\")");
        }
        try {
            return Integer.valueOf(m.group(1));
        } catch (NumberFormatException e) {
            throw new ValorInvalidoException("Opção inválida para o campo '" + campo + "'");
        }
    }

    /** Igual a {@link #idOuNulo}, mas para campos obrigatórios. */
    public static Integer id(String valor, String campo) {
        Integer id = idOuNulo(valor, campo);
        if (id == null) {
            throw new ValorInvalidoException("O campo '" + campo + "' é obrigatório");
        }
        return id;
    }
}
