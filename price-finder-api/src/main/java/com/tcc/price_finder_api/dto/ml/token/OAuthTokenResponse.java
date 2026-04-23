package com.tcc.price_finder_api.dto.ml.token;

import lombok.Data;

// DTO para a resposta da api que devolve o token do mercado livre

@Data
public class OAuthTokenResponse {

    private String access_token;
    private String token_type;
    private Long expires_in;
    private String refresh_token;
}
