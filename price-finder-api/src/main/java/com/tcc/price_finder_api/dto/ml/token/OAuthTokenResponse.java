package com.tcc.price_finder_api.dto.ml.token;

import lombok.Data;

@Data
public class OAuthTokenResponse {

    private String access_token;
    private String token_type;
    private Long expires_in;
    private String refresh_token;
}
