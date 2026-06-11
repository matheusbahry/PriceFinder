package com.tcc.price_finder_api.controller.ml;

import com.tcc.price_finder_api.model.api.MercadoLivreToken;
import com.tcc.price_finder_api.service.ml.MercadoLivreAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class MLTokenController {

    private final MercadoLivreAuthService service;

    @GetMapping("/token/{id}")
    public Mono<String> getToken(@PathVariable UUID id) {

        return service.getValidAccessToken(id);

    }

    @PostMapping("/{id}/refresh")
    public Mono<MercadoLivreToken> refresh(@PathVariable UUID id) {
        return service.refreshToken(id);
    }
}
