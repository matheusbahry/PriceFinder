package com.tcc.price_finder_api.dto.ml.search;

public record Paging(
        int total,
        int primary_results,
        int offset,
        int limit
) {}