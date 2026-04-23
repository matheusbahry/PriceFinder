package com.tcc.price_finder_api.dto.security;

public record AuthResponseDTO(
        String token,
        String role,
        UserResponseDTO userResponseDTO
) {}
