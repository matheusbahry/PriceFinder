package com.tcc.price_finder_api.model;

import com.tcc.price_finder_api.dto.ml.search.SearchItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("product")
public class Product {

    @Id
    private String id;

    private String title;

    private BigDecimal price;

    private BigDecimal originalPrice;

    private String currencyId;

    private String permalink;

    private String thumbnail;

    private String condition;

    private Boolean freeShipping;

    private Integer availableQuantity;

    private Long sellerId;

    private String categoryId;

    private Instant createdAt;

    private Instant updatedAt;

    public static Product fromSearchItem(SearchItem item) {

        return Product.builder()
                .id(item.id())
                .title(item.title())
                .price(item.price())
                .originalPrice(item.original_price())
                .currencyId(item.currency_id())
                .permalink(item.permalink())
                .thumbnail(item.thumbnail())
                .condition(item.condition())
                .availableQuantity(item.available_quantity())
                .sellerId(item.seller() != null ? item.seller().id() : null)
                .categoryId(item.category_id())
                .freeShipping(
                        item.shipping() != null
                                ? item.shipping().free_shipping()
                                : null
                )
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}
