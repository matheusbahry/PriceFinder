package com.tcc.price_finder_api.model.api;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Data
@Table("mercado_livre_token")
public class MercadoLivreToken {

    @Id
    private UUID id;

    private String accessToken;
    private String refreshToken;

    private Instant expiresAt;
}
