package com.tcc.price_finder_api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${mbl.website}")
    private String MBL_WEBSITE;

    @Value("${ebay.website}")
    private String EBAY_WEBSITE;

    @Bean
    public WebClient mercadoLivreWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(MBL_WEBSITE)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Bean
    public WebClient ebayWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(EBAY_WEBSITE)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
