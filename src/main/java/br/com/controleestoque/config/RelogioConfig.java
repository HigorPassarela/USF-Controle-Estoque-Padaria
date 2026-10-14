package br.com.controleestoque.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Relógio da aplicação no fuso da padaria. Define o que é "hoje" para a validade dos lotes
 * (um lote vence quando a data de validade fica antes de hoje) e a data e hora das movimentações.
 */
@Configuration
public class RelogioConfig {

    public static final ZoneId FUSO_PADARIA = ZoneId.of("America/Sao_Paulo");

    @Bean
    public Clock relogio() {
        return Clock.system(FUSO_PADARIA);
    }
}
