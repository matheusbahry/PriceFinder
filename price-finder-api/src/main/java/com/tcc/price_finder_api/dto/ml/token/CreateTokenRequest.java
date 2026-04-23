package com.tcc.price_finder_api.dto.ml.token;

// DTO para a requisição do token

public record CreateTokenRequest(
        String accessToken,
        String refreshToken,
        long expiresIn
) {}
