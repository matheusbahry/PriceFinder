package com.tcc.price_finder_api.service.ml;

import com.tcc.price_finder_api.config.MercadoLivreConfig;
import com.tcc.price_finder_api.dto.ml.token.OAuthTokenResponse;
import com.tcc.price_finder_api.model.api.MercadoLivreToken;
import com.tcc.price_finder_api.repo.api.MercadoLivreTokenRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Service
public class MercadoLivreAuthService {

    private final WebClient webClient;
    private final MercadoLivreConfig config;
    private final MercadoLivreTokenRepository repository;

    @Value("${mlb.auth-token}")
    private String AUTH_TOKEN_URL;

    public MercadoLivreAuthService(@Qualifier("mercadoLivreWebClient") WebClient webClient, MercadoLivreConfig config, MercadoLivreTokenRepository repository) {
        this.webClient = webClient;
        this.config = config;
        this.repository = repository;
    }

    public Mono<MercadoLivreToken> refreshToken(UUID id) {

        return repository.findById(id)
                .switchIfEmpty(Mono.error(
                        new RuntimeException("Token não encontrado")
                ))
                .flatMap(saved ->
                        webClient.post()
                                .uri(AUTH_TOKEN_URL)
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .body(BodyInserters.fromFormData("grant_type", "refresh_token")
                                        .with("client_id", config.getAppId())
                                        .with("client_secret", config.getSecretKey())
                                        .with("refresh_token", saved.getRefreshToken()))
                                .retrieve()
                                .bodyToMono(OAuthTokenResponse.class)
                                .flatMap(resp -> updateAndSave(saved, resp))
                );
    }

    private Mono<MercadoLivreToken> updateAndSave(
            MercadoLivreToken token,
            OAuthTokenResponse resp
    ) {
        token.setAccessToken(resp.getAccess_token());
        token.setRefreshToken(resp.getRefresh_token());
        token.setExpiresAt(
                Instant.now().plusSeconds(resp.getExpires_in())
        );

        return repository.save(token);
    }

    public Mono<String> getValidAccessToken(UUID id) {

        return repository.findById(id)
                .switchIfEmpty(Mono.error(
                        new RuntimeException("Token não encontrado")
                ))
                .flatMap(token -> {

                    boolean valid =
                            token.getExpiresAt().isAfter(Instant.now());

                    if (valid) {
                        return Mono.just(token.getAccessToken());
                    }

                    return refreshToken(id)
                            .map(MercadoLivreToken::getAccessToken);
                });
    }

    @Scheduled(fixedDelay = 300000)
    public void refreshNearExpiration() {

        repository.findAll()
                .filter(t ->
                        t.getExpiresAt()
                                .isBefore(Instant.now().plusSeconds(600))
                )
                .flatMap(t -> refreshToken(t.getId()))
                .subscribe();
    }
}