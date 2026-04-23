package com.tcc.price_finder_api.service;

import com.tcc.price_finder_api.dto.ml.search.SearchItem;
import com.tcc.price_finder_api.dto.ml.search.SearchResponse;
import com.tcc.price_finder_api.model.Product;
import com.tcc.price_finder_api.repo.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
public class ProductService {

    private final ProductRepository repository;

    private final WebClient webClient;

    public ProductService(@Qualifier("ebayWebClient") WebClient webClient, ProductRepository repository) {
        this.webClient = webClient;
        this.repository = repository;
    }

    // 🔎 Buscar produtos na API e salvar localmente
    public Flux<Product> searchAndSave(String query) {

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/sites/MLB/search")
                        .queryParam("q", query)
                        .build())
                .retrieve()
                .bodyToMono(SearchResponse.class)
                .flatMapMany(response ->
                        Flux.fromIterable(response.results()))
                .map(this::mapToEntity)
                .flatMap(repository::save);
    }

    // 📦 Buscar todos os produtos salvos
    public Flux<Product> findAll() {
        return repository.findAll();
    }

    // 🔎 Buscar por ID
    public Mono<Product> findById(String id) {
        return repository.findById(id);
    }

    // 🗂️ Buscar por categoria
    public Flux<Product> findByCategory(String categoryId) {
        return repository.findByCategoryId(categoryId);
    }

    // 💾 Salvar manualmente
    public Mono<Product> save(Product product) {
        product.setUpdatedAt(Instant.now());
        return repository.save(product);
    }

    // ❌ Remover
    public Mono<Void> delete(String id) {
        return repository.deleteById(id);
    }

    // 🏷️ Detectar promoções (desconto)
    public Flux<Product> findPromotions() {
        return repository.findAll()
                .filter(p ->
                        p.getOriginalPrice() != null &&
                                p.getOriginalPrice().compareTo(p.getPrice()) > 0
                );
    }

    // 🔄 Mapper DTO → Entidade
    private Product mapToEntity(SearchItem item) {

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
                .sellerId(
                        item.seller() != null
                                ? item.seller().id()
                                : null
                )
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