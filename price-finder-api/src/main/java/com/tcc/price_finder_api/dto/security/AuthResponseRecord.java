package com.tcc.price_finder_api.dto.security;


public record AuthResponseRecord(String token, String role, UserResponseDTO userResponseDTO) {
}
