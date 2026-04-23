package com.tcc.price_finder_api.service.ebay;

import com.tcc.price_finder_api.dto.ebay.EbayItemSummary;
import com.tcc.price_finder_api.dto.ebay.EbaySearchResponse;
import com.tcc.price_finder_api.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

@Service
public class EbayProductService {

    private final WebClient webClient;
    private final EbayAuthService authService;

    public EbayProductService(
            @Qualifier("ebayWebClient") WebClient webClient,
            EbayAuthService authService
    ) {
        this.webClient = webClient;
        this.authService = authService;
    }

    private static final String BASE_SEARCH =
            "/buy/browse/v1/item_summary/search";

    // ===============================
    // 🔎 Buscar por palavra-chave
    // ===============================

    public Flux<Product> searchByKeyword(String keyword) {

        return authService.getValidToken()
                .flatMapMany(token ->
                        webClient.get()
                                .uri(uriBuilder -> uriBuilder
                                        .path(BASE_SEARCH)
                                        .queryParam("q", keyword)
                                        .queryParam("limit", 50)
                                        .build())
                                .headers(h -> h.setBearerAuth(token))
                                .retrieve()
                                .bodyToMono(EbaySearchResponse.class)
                )
                .flatMapIterable(EbaySearchResponse::itemSummaries)
                .map(this::mapToProduct);
    }

    // ===============================
    // 💰 Buscar por faixa de preço
    // ===============================

    public Flux<Product> searchByPriceRange(
            String keyword,
            double min,
            double max
    ) {

        String filter = "price:[" + min + ".." + max + "]";

        return authService.getValidToken()
                .flatMapMany(token ->
                        webClient.get()
                                .uri(uriBuilder -> uriBuilder
                                        .path(BASE_SEARCH)
                                        .queryParam("q", keyword)
                                        .queryParam("filter", filter)
                                        .queryParam("limit", 50)
                                        .build())
                                .headers(h -> h.setBearerAuth(token))
                                .retrieve()
                                .bodyToMono(EbaySearchResponse.class)
                )
                .flatMapIterable(EbaySearchResponse::itemSummaries)
                .map(this::mapToProduct);
    }

    // ===============================
    // ⭐ Buscar ordenado
    // ===============================

    public Flux<Product> searchSorted(
            String keyword,
            String sort // price, -price, bestMatch, newlyListed
    ) {

        return authService.getValidToken()
                .flatMapMany(token ->
                        webClient.get()
                                .uri(uriBuilder -> uriBuilder
                                        .path(BASE_SEARCH)
                                        .queryParam("q", keyword)
                                        .queryParam("sort", sort)
                                        .queryParam("limit", 50)
                                        .build())
                                .headers(h -> h.setBearerAuth(token))
                                .retrieve()
                                .bodyToMono(EbaySearchResponse.class)
                )
                .flatMapIterable(EbaySearchResponse::itemSummaries)
                .map(this::mapToProduct);
    }

    // ===============================
    // 🆔 Buscar detalhes por ID
    // ===============================

    public Mono<Product> getById(String itemId) {

        return authService.getValidToken()
                .flatMap(token ->
                        webClient.get()
                                .uri("/buy/browse/v1/item/" + itemId)
                                .headers(h -> h.setBearerAuth(token))
                                .retrieve()
                                .bodyToMono(EbayItemSummary.class)
                )
                .map(this::mapToProduct);
    }

    // ===============================
    // 🧠 Conversão DTO → Entity
    // ===============================

    private Product mapToProduct(EbayItemSummary item) {

        return Product.builder()
                .id(item.itemId())
                .title(item.title())
                .price(item.price().value())
                .currencyId(item.price().currency())
                .permalink(item.itemWebUrl())
                .thumbnail(item.image() != null
                        ? item.image().imageUrl()
                        : null)
                .condition(item.condition())
                .availableQuantity(null)
                .sellerId(null)
                .categoryId(item.categoryId())
                .freeShipping(
                        item.shippingOptions() != null &&
                                item.shippingOptions().stream()
                                        .anyMatch(s ->
                                                Boolean.TRUE.equals(
                                                        s.freeShipping()))
                )
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}