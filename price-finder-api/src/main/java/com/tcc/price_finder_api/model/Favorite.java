package com.tcc.price_finder_api.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table("favorites")
public class Favorite {

    @Id
    private UUID id;

    @Column("user_id")
    private UUID userId;

    @Column("product_name")
    private String productName;

    @Column("product_url")
    private String productUrl;

    @Column("created_at")
    private Instant createdAt;
}