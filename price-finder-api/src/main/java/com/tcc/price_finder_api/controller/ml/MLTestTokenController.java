package com.tcc.price_finder_api.controller.ml;

import com.tcc.price_finder_api.dto.ml.token.CreateTokenRequest;
import com.tcc.price_finder_api.model.api.MercadoLivreToken;
import com.tcc.price_finder_api.repo.api.MercadoLivreTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;


@RestController
@RequiredArgsConstructor
@RequestMapping("/test")
public class MLTestTokenController {

    private final MercadoLivreTokenRepository repository;

    @PostMapping("/token")
    public Mono<MercadoLivreToken> createTestToken(
            @RequestBody CreateTokenRequest req
    ) {

        MercadoLivreToken token = new MercadoLivreToken();

        //token.setId(UUID.randomUUID());
        token.setAccessToken(req.accessToken());
        token.setRefreshToken(req.refreshToken());
        token.setExpiresAt(
                Instant.now().plusSeconds(req.expiresIn())
        );

        return repository.save(token);
    }

    @GetMapping("/all")
    public Flux<MercadoLivreToken> all() {
        return repository.findAll();
    }
}