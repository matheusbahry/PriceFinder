package com.tcc.price_finder_api.repo;

import com.tcc.price_finder_api.model.Favorite;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface FavoriteRepository extends ReactiveCrudRepository<Favorite, UUID> {

    Flux<Favorite> findAllByUserId(UUID userId);

    Mono<Favorite> findByUserIdAndProductUrl(UUID userId, String productUrl);

}
