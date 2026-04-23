package com.tcc.price_finder_api.dto.security;

import java.util.UUID;

public record AuthenticatedUser(
        UUID id,
        String email
) {}

