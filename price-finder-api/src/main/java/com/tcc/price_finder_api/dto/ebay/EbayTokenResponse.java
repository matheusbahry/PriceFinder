package com.tcc.price_finder_api.dto.ebay;

public record EbayTokenResponse(
        String access_token,
        long expires_in,
        String token_type
) {
    public String accessToken() { return access_token; }
    public long expiresIn() { return expires_in; }
}
