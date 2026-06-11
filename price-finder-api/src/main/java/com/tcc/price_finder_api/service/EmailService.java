package com.tcc.price_finder_api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    private final WebClient webClient;

    @Value("${resend.api-key}")
    private String apiKey;

    public EmailService(
            WebClient.Builder builder,
            @Value("${resend.URL}") String resendUrl
    ) {
        this.webClient = builder
                .baseUrl(resendUrl)
                .build();
    }

    public Mono<Void> sendCode(String to, String code) {

        Map<String, Object> body = Map.of(
                "from", "onboarding@resend.dev",
                "to", List.of(to),
                "subject", "Seu código OTP",
                "html",
                """
                <h2>Seu código é:</h2>
                <h1>%s</h1>
                <p>Expira em 5 minutos.</p>
                """.formatted(code)
        );

        return webClient.post()
                .uri("/emails")
                .header("Authorization", "Bearer " + apiKey)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Void.class);
    }
}