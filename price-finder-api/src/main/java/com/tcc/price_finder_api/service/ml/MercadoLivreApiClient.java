package com.tcc.price_finder_api.service.ml;

import com.tcc.price_finder_api.dto.ml.search.ItemDetails;
import com.tcc.price_finder_api.dto.ml.search.SearchResponse;
import com.tcc.price_finder_api.dto.ml.search.UserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class MercadoLivreApiClient {

    private final WebClient webClient;

    @Value("${mbl.search}")
    private String SEARCH_URL;

    @Value("${mbl.itens}")
    private String ITENS_URL;

    @Value("${mbl.users}")
    private String USERS_URL;

    public MercadoLivreApiClient(@Qualifier("mercadoLivreWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<SearchResponse> search(String query) {

        return webClient.get()
                .uri(uri -> uri
                        .path(SEARCH_URL)
                        .queryParam("q", query)
                        .build())
                .retrieve()
                .bodyToMono(SearchResponse.class);
    }

    public Mono<SearchResponse> searchByPrice(String priceRange) {

        return webClient.get()
                .uri(uri -> uri
                        .path(SEARCH_URL)
                        .queryParam("price", priceRange)
                        .build())
                .retrieve()
                .bodyToMono(SearchResponse.class);
    }

    public Mono<SearchResponse> searchByCategory(String categoryId) {

        return webClient.get()
                .uri(uri -> uri
                        .path(SEARCH_URL)
                        .queryParam("category", categoryId)
                        .build())
                .retrieve()
                .bodyToMono(SearchResponse.class);
    }

    public Mono<ItemDetails> getItem(String itemId) {

        return webClient.get()
                .uri(ITENS_URL, itemId)
                .retrieve()
                .bodyToMono(ItemDetails.class);
    }

    public Mono<UserInfo> getUser(Long userId) {

        return webClient.get()
                .uri(USERS_URL, userId)
                .retrieve()
                .bodyToMono(UserInfo.class);
    }
}
