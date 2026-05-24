package com.tcc.price_finder_api.service.ml;

import com.tcc.price_finder_api.dto.ml.search.ItemDetails;
import com.tcc.price_finder_api.dto.ml.search.SearchResponse;
import com.tcc.price_finder_api.dto.ml.search.UserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class MercadoLivreApiClient {

    private final WebClient webClient;

    public MercadoLivreApiClient(@Qualifier("mercadoLivreWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    // 🔎 Busca produtos
    public Mono<SearchResponse> search(String query) {

        return webClient.get()
                .uri(uri -> uri
                        .path("/sites/MLB/search")
                        .queryParam("q", query)
                        .build())
                .retrieve()
                .bodyToMono(SearchResponse.class);
    }

    // 💰 Busca por faixa de preço
    public Mono<SearchResponse> searchByPrice(String priceRange) {

        return webClient.get()
                .uri(uri -> uri
                        .path("/sites/MLB/search")
                        .queryParam("price", priceRange)
                        .build())
                .retrieve()
                .bodyToMono(SearchResponse.class);
    }

    // 🗂️ Busca por categoria
    public Mono<SearchResponse> searchByCategory(String categoryId) {

        return webClient.get()
                .uri(uri -> uri
                        .path("/sites/MLB/search")
                        .queryParam("category", categoryId)
                        .build())
                .retrieve()
                .bodyToMono(SearchResponse.class);
    }

    // 🆔 Detalhes do produto
    public Mono<ItemDetails> getItem(String itemId) {

        return webClient.get()
                .uri("/items/{id}", itemId)
                .retrieve()
                .bodyToMono(ItemDetails.class);
    }

    // 🏪 Dados do vendedor
    public Mono<UserInfo> getUser(Long userId) {

        return webClient.get()
                .uri("/users/{id}", userId)
                .retrieve()
                .bodyToMono(UserInfo.class);
    }
}
