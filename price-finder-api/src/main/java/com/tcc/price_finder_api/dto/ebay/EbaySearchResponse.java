package com.tcc.price_finder_api.dto.ebay;

import java.util.List;

public record EbaySearchResponse(
        List<EbayItemSummary> itemSummaries
) {}