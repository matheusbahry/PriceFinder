package com.tcc.price_finder_api.dto.ml.search;

public record SellerReputation(

        String level_id,
        PowerSellerStatus power_seller_status,
        Transactions transactions

) {}
