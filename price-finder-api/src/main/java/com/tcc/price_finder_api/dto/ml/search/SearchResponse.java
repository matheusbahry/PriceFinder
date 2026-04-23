package com.tcc.price_finder_api.dto.ml.search;

import java.util.List;

public record SearchResponse(
        String site_id,
        String query,
        Paging paging,
        List<SearchItem> results
) {}
