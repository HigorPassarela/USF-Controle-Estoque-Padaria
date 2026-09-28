package br.com.controleestoque.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI controleEstoqueOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("USF - Controle de Estoque API")
                        .description("API de controle de estoque e vendas: cadastros, pedidos, lotes e movimentações em uma Padaria.")
                        .version("v0.0.1"));
    }
}
