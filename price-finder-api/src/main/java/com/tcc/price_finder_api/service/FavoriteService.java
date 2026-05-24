package com.tcc.price_finder_api.service;

import com.tcc.price_finder_api.model.Favorite;
import com.tcc.price_finder_api.repo.FavoriteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;

    public Mono<Favorite> save(
            UUID userId,
            String productName,
            String productUrl
    ) {

        log.info(
                "Tentando salvar favorito | userId={} | productName={} | productUrl={}",
                userId,
                productName,
                productUrl
        );

        return favoriteRepository
                .findByUserIdAndProductUrl(
                        userId,
                        productUrl
                )

                .flatMap(favorite ->
                        Mono.error(
                                new RuntimeException(
                                        "Produto já está nos favoritos"
                                )
                        )
                )

                .switchIfEmpty(
                        Mono.defer(() -> {

                            Favorite favorite =
                                    Favorite.builder()

                                            .userId(userId)

                                            .productName(productName)

                                            .productUrl(productUrl)

                                            .createdAt(Instant.now())

                                            .build();

                            log.info(
                                    "Salvando novo favorito | favorite={}",
                                    favorite
                            );

                            return favoriteRepository.save(
                                    favorite
                            );
                        })
                )

                .cast(Favorite.class)

                .doOnNext(saved ->
                        log.info(
                                "Favorito salvo com sucesso | favorite={}",
                                saved
                        )
                )

                .doOnError(error ->
                        log.error(
                                "Erro ao salvar favorito",
                                error
                        )
                );
    }

    public Flux<Favorite> findByUserId(UUID userId) {

        return favoriteRepository.findAllByUserId(userId);
    }

    public Mono<Void> remove(
            UUID userId,
            String productUrl
    ) {

        return favoriteRepository
                .findByUserIdAndProductUrl(
                        userId,
                        productUrl
                )
                .flatMap(favoriteRepository::delete);
    }

    public Mono<Boolean> isFavorite(
            UUID userId,
            String productUrl
    ) {

        return favoriteRepository
                .findByUserIdAndProductUrl(
                        userId,
                        productUrl
                )
                .hasElement();
    }
}