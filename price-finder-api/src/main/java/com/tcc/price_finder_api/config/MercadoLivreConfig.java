package com.tcc.price_finder_api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "mercado-livre")
public class MercadoLivreConfig {

    private String appId;
    private String secretKey;
    private String apiUri;
    private String redirectApi;
    private String authorizationCode;
    private String refreshToken;
}