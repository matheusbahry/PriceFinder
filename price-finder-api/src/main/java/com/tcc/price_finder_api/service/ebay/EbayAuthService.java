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

    public EbayAuthService(
            @Qualifier("ebayWebClient") WebClient webClient
    ) {
        this.webClient = webClient;
    }

    private final AtomicReference<TokenHolder> tokenCache =
            new AtomicReference<>();

    // =========================================
    // 🚀 BUSCA TOKEN AO INICIAR A APLICAÇÃO
    // =========================================

    @PostConstruct
    public void init() {
        requestNewToken()
                .doOnNext(tokenCache::set)
                .subscribe(); // necessário em código imperativo
    }

    // =========================================
    // ⏱️ RENOVAÇÃO AUTOMÁTICA
    // =========================================

    @Scheduled(fixedDelay = 300_000) // a cada 5 minutos
    public void refreshTokenIfNeeded() {

        TokenHolder holder = tokenCache.get();

        if (holder == null || holder.isAboutToExpire()) {
            requestNewToken()
                    .doOnNext(tokenCache::set)
                    .subscribe();
        }
    }

    // =========================================
    // 🔐 OBTER TOKEN ATUAL
    // =========================================

    public Mono<String> getValidToken() {

        TokenHolder holder = tokenCache.get();

        if (holder != null && !holder.isExpired()) {
            return Mono.just(holder.accessToken());
        }

        return requestNewToken()
                .doOnNext(tokenCache::set)
                .map(TokenHolder::accessToken);
    }

    // =========================================
    // 🌐 CHAMADA OAuth eBay
    // =========================================

    private Mono<TokenHolder> requestNewToken() {

        String basicAuth = Base64.getEncoder().encodeToString(
                (clientId + ":" + clientSecret)
                        .getBytes(StandardCharsets.UTF_8)
        );

        return webClient.post()
                .uri("https://api.ebay.com/identity/v1/oauth2/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header("Authorization", "Basic " + basicAuth)
                .bodyValue(
                        "grant_type=client_credentials" +
                                "&scope=https://api.ebay.com/oauth/api_scope"
                )
                .retrieve()
                .bodyToMono(EbayTokenResponse.class)
                .map(resp -> new TokenHolder(
                        resp.accessToken(),
                        Instant.now().plusSeconds(resp.expiresIn())
                ));
    }

    @Scheduled(fixedRate = 60 * 60 * 1000) // 1 hora
    public void refreshTokenHourly() {

        requestNewToken()
                .doOnNext(token -> {
                    tokenCache.set(token);
                    System.out.println("🔄 Token eBay renovado automaticamente");
                })
                .doOnError(err ->
                        System.err.println("❌ Falha ao renovar token: " + err.getMessage()))
                .subscribe(); // 🔥 ESSENCIAL em WebFlux
    }

}
