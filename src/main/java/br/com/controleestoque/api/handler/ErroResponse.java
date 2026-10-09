package br.com.controleestoque.api.handler;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErroResponse(
        int status,
        String erro,
        String mensagem,
        String caminho,
        Instant timestamp,
        List<CampoInvalido> campos
) {
    public record CampoInvalido(String campo, String mensagem) {
    }

    public static ErroResponse of(int status, String erro, String mensagem, String caminho) {
        return new ErroResponse(status, erro, mensagem, caminho, Instant.now(), null);
    }
}
