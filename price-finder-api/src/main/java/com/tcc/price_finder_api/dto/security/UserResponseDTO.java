package com.tcc.price_finder_api.dto.security;


import com.tcc.price_finder_api.enums.auth.UserStatus;
import com.tcc.price_finder_api.model.auth.User;

import java.time.Instant;
import java.util.UUID;

public record UserResponseDTO(
        UUID id,
        String email,
        UserStatus status,
        Instant createdAt
) {
    public static UserResponseDTO from(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getEmail(),
                user.getStatus(),
                user.getCreatedAt()
        );
    }
}

