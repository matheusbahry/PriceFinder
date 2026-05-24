package com.tcc.price_finder_api.dto;

import java.util.UUID;

public record FavoriteRequestDTO(

        UUID userId,

        String productName,

        String productUrl

) {}
