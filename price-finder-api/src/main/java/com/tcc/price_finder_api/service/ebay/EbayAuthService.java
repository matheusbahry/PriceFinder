package com.tcc.price_finder_api.service.ebay;

import com.tcc.price_finder_api.dto.ebay.EbayTokenResponse;
import com.tcc.price_finder_api.dto.ebay.TokenHolder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class EbayAuthService {

    private final WebClient webClient;

    @Value("${ebay.api.client-id}")
    private String clientId;

    @Value("${ebay.api.client-secret}")
    private String clientSecret;

    /**
     * Path relativo ao baseUrl "https://api.ebay.com":
     * ebay.url.auth-token=/identity/v1/oauth2/token
     */
    @Value("${ebay.url.auth-token}")
    private String AUTH_TOKEN;

    /**
     * Escopo enviado no body da requisição de token:
     * ebay.url.auth-scope=https://api.ebay.com/oauth/api_scope
     */
    @Value("${ebay.url.auth-scope}")
    private String AUTH_SCOPE;

    public EbayAuthService(
            @Qualifier("ebayWebClient") WebClient webClient
    ) {
        this.webClient = webClient;
    }

    private final AtomicReference<TokenHolder> tokenCache =
            new AtomicReference<>();

    @PostConstruct
    public void init() {
        requestNewToken()
                .doOnNext(tokenCache::set)
                .subscribe();
    }

    @Scheduled(fixedDelay = 300_000)
    public void refreshTokenIfNeeded() {
        TokenHolder holder = tokenCache.get();
        if (holder == null || holder.isAboutToExpire()) {
            requestNewToken()
                    .doOnNext(tokenCache::set)
                    .subscribe();
        }
    }

    public Mono<String> getValidToken() {
        TokenHolder holder = tokenCache.get();
        if (holder != null && !holder.isExpired()) {
            return Mono.just(holder.accessToken());
        }
        return requestNewToken()
                .doOnNext(tokenCache::set)
                .map(TokenHolder::accessToken);
    }

    private Mono<TokenHolder> requestNewToken() {
        String basicAuth = Base64.getEncoder().encodeToString(
                (clientId + ":" + clientSecret)
                        .getBytes(StandardCharsets.UTF_8)
        );

        return webClient.post()
                .uri(AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header("Authorization", "Basic " + basicAuth)
                .bodyValue(
                        "grant_type=client_credentials" +
                                "&scope=" + AUTH_SCOPE
                )
                .retrieve()
                .bodyToMono(EbayTokenResponse.class)
                .map(resp -> new TokenHolder(
                        resp.accessToken(),
                        Instant.now().plusSeconds(resp.expiresIn())
                ));
    }

    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void refreshTokenHourly() {
        requestNewToken()
                .doOnNext(token -> {
                    tokenCache.set(token);
                    System.out.println("Token eBay renovado automaticamente");
                })
                .doOnError(err ->
                        System.err.println("Falha ao renovar token: " + err.getMessage()))
                .subscribe();
    }
}