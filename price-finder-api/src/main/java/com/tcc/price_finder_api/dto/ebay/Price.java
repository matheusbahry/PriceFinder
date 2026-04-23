package com.tcc.price_finder_api.dto.ebay;

import java.math.BigDecimal;

public record Price(
        BigDecimal value,
        String currency
) {}
