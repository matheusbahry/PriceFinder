package com.tcc.price_finder_api.dto.ebay;

import java.time.Instant;

public record TokenHolder(
        String accessToken,
        Instant expiresAt
) {

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isAboutToExpire() {
        return Instant.now()
                .isAfter(expiresAt.minusSeconds(120)); // margem 2 min
    }
}
