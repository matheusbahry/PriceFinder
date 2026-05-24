package com.tcc.price_finder_api.controller;

import com.tcc.price_finder_api.dto.FavoriteRequestDTO;
import com.tcc.price_finder_api.model.Favorite;
import com.tcc.price_finder_api.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Favorite> save(
            @RequestBody FavoriteRequestDTO request
    ) {

        log.info("DTO recebido: {}", request);

        log.info(
                "Campos recebidos | userId={} | productName={} | productUrl={}",
                request.userId(),
                request.productName(),
                request.productUrl()
        );

        return favoriteService.save(
                request.userId(),
                request.productName(),
                request.productUrl()
        );
    }

    @GetMapping("/{userId}")
    public Flux<Favorite> findByUserId(
            @PathVariable UUID userId
    ) {

        return favoriteService.findByUserId(userId);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> remove(
            @RequestBody FavoriteRequestDTO request
    ) {

        return favoriteService.remove(
                request.userId(),
                request.productUrl()
        );
    }

    @GetMapping("/check")
    public Mono<Boolean> isFavorite(

            @RequestParam UUID userId,

            @RequestParam String productUrl
    ) {

        return favoriteService.isFavorite(
                userId,
                productUrl
        );
    }
}