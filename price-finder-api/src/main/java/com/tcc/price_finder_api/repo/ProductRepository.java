package com.tcc.price_finder_api.repo;

import com.tcc.price_finder_api.model.Product;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface ProductRepository
        extends ReactiveCrudRepository<Product, String> {

    Flux<Product> findByCategoryId(String categoryId);
}