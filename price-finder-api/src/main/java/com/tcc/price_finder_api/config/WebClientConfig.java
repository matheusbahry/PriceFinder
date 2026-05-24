package com.tcc.price_finder_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

// Entidade que representa as configurações do "client" para consumir a api do mercado livre

@Configuration
public class WebClientConfig {

    // 🟡 Mercado Livre
    @Bean
    public WebClient mercadoLivreWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl("https://api.mercadolibre.com")
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    // 🔵 eBay
    @Bean
    public WebClient ebayWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl("https://api.ebay.com")
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
