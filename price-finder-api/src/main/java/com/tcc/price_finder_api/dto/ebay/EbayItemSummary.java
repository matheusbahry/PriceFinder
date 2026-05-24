package com.tcc.price_finder_api.dto.ebay;

import java.awt.*;
import java.util.List;

public record EbayItemSummary(
        String itemId,
        String title,
        Price price,
        String itemWebUrl,
        Image image,
        String condition,
        String categoryId,
        List<ShippingOption> shippingOptions
) {}