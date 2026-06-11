package com.tcc.price_finder_api.dto.ml.token;
public record CreateTokenRequest(
        String accessToken,
        String refreshToken,
        long expiresIn
) {}
