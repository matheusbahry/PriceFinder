package com.tcc.price_finder_api.dto.ml.search;

public record UserInfo(

        Long id,
        String nickname,
        String permalink,
        String registration_date,
        String country_id,
        String user_type,
        SellerReputation seller_reputation

) {}
