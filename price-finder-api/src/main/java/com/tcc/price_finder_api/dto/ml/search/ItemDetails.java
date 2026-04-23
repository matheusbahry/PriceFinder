package com.tcc.price_finder_api.dto.ml.search;

import java.math.BigDecimal;
import java.util.List;

public record ItemDetails(

        String id,
        String title,
        BigDecimal price,
        BigDecimal original_price,
        String currency_id,
        String permalink,
        String thumbnail,
        String condition,
        Integer available_quantity,
        String category_id,
        Seller seller_id,
        Shipping shipping,
        List<Picture> pictures

) {}
