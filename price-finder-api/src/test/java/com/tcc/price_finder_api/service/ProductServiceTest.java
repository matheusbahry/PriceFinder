package com.tcc.price_finder_api.service;

import com.tcc.price_finder_api.dto.ml.search.SearchItem;
import com.tcc.price_finder_api.dto.ml.search.SearchResponse;
import com.tcc.price_finder_api.dto.ml.search.Seller;
import com.tcc.price_finder_api.dto.ml.search.Shipping;
import com.tcc.price_finder_api.model.Product;
import com.tcc.price_finder_api.repo.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService")
class ProductServiceTest {

    // --- Mocks da cadeia do WebClient ---
    @Mock private WebClient webClient;
    @Mock private WebClient.RequestHeadersUriSpec<?> requestHeadersUriSpec;
    @Mock private WebClient.RequestHeadersSpec<?> requestHeadersSpec;
    @Mock private WebClient.ResponseSpec responseSpec;

    @Mock private ProductRepository repository;

    private ProductService productService;

    // -----------------------------------------------------------------------
    // Helpers de fixture
    // -----------------------------------------------------------------------

    private Seller seller(Long id) {
        return new Seller(id);
    }

    private Shipping shipping(boolean free) {
        return new Shipping(free);
    }

    private SearchItem buildSearchItem(String id, String title,
                                       BigDecimal price, BigDecimal originalPrice,
                                       Seller seller, Shipping shipping) {
        // Ordem dos campos conforme o record SearchItem:
        // id, title, price, original_price, currency_id, permalink,
        // thumbnail, condition, available_quantity, category_id, seller, shipping
        return new SearchItem(
                id,
                title,
                price,
                originalPrice,
                "BRL",
                "https://permalink/" + id,
                "https://thumb/" + id,
                "new",
                10,
                "MLB1234",
                seller,
                shipping
        );
    }

    private Product buildProduct(String id, BigDecimal price, BigDecimal originalPrice) {
        return Product.builder()
                .id(id)
                .title("Product " + id)
                .price(price)
                .originalPrice(originalPrice)
                .currencyId("BRL")
                .categoryId("MLB1234")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    // -----------------------------------------------------------------------
    // Setup
    // -----------------------------------------------------------------------

    @BeforeEach
    void setUp() {
        productService = new ProductService(webClient, repository);
    }

    // -----------------------------------------------------------------------
    // Configuração reutilizável da cadeia do WebClient
    // -----------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private void setupWebClientChain(SearchResponse response) {
        doReturn(requestHeadersUriSpec).when(webClient).get();
        doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(any(java.util.function.Function.class));
        doReturn(responseSpec).when(requestHeadersSpec).retrieve();
        doReturn(Mono.just(response)).when(responseSpec).bodyToMono(SearchResponse.class);
    }

    // =======================================================================
    // searchAndSave
    // =======================================================================

    @Nested
    @DisplayName("searchAndSave()")
    class SearchAndSave {

        @Test
        @DisplayName("deve buscar itens, mapear para entidade e salvar no repositório")
        void shouldFetchMapAndSaveProducts() {
            SearchItem item1 = buildSearchItem("MLB1", "Notebook", new BigDecimal("3000"), null,
                    seller(42L), shipping(true));
            SearchItem item2 = buildSearchItem("MLB2", "Mouse", new BigDecimal("150"), null,
                    null, null);

            SearchResponse response = new SearchResponse("MLB", "notebook", null, List.of(item1, item2));
            setupWebClientChain(response);

            Product p1 = buildProduct("MLB1", new BigDecimal("3000"), null);
            Product p2 = buildProduct("MLB2", new BigDecimal("150"), null);

            when(repository.save(any(Product.class)))
                    .thenReturn(Mono.just(p1))
                    .thenReturn(Mono.just(p2));

            StepVerifier.create(productService.searchAndSave("notebook"))
                    .expectNextMatches(p -> "MLB1".equals(p.getId()))
                    .expectNextMatches(p -> "MLB2".equals(p.getId()))
                    .verifyComplete();

            verify(repository, times(2)).save(any(Product.class));
        }

        @Test
        @DisplayName("deve retornar Flux vazio quando API não retorna resultados")
        void shouldReturnEmptyFluxWhenNoResults() {
            SearchResponse emptyResponse = new SearchResponse("MLB", "xyz", null, List.of());
            setupWebClientChain(emptyResponse);

            StepVerifier.create(productService.searchAndSave("xyz"))
                    .verifyComplete();

            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("deve mapear seller nulo sem lançar exceção")
        void shouldHandleNullSeller() {
            SearchItem item = buildSearchItem("MLB3", "Teclado", new BigDecimal("200"), null,
                    null, shipping(false));
            SearchResponse response = new SearchResponse("MLB", "teclado", null, List.of(item));
            setupWebClientChain(response);

            Product saved = buildProduct("MLB3", new BigDecimal("200"), null);
            when(repository.save(any(Product.class))).thenReturn(Mono.just(saved));

            StepVerifier.create(productService.searchAndSave("teclado"))
                    .expectNextMatches(p -> p.getSellerId() == null)
                    .verifyComplete();
        }

        @Test
        @DisplayName("deve mapear freeShipping corretamente quando shipping é nulo")
        void shouldHandleNullShipping() {
            SearchItem item = buildSearchItem("MLB4", "Monitor", new BigDecimal("800"), null,
                    seller(99L), null);
            SearchResponse response = new SearchResponse("MLB", "monitor", null, List.of(item));
            setupWebClientChain(response);

            Product saved = buildProduct("MLB4", new BigDecimal("800"), null);
            when(repository.save(any(Product.class))).thenReturn(Mono.just(saved));

            StepVerifier.create(productService.searchAndSave("monitor"))
                    .expectNextMatches(p -> p.getFreeShipping() == null)
                    .verifyComplete();
        }

        @Test
        @DisplayName("deve propagar erro do WebClient")
        @SuppressWarnings("unchecked")
        void shouldPropagateWebClientError() {
            doReturn(requestHeadersUriSpec).when(webClient).get();
            doReturn(requestHeadersSpec).when(requestHeadersUriSpec).uri(any(java.util.function.Function.class));
            doReturn(responseSpec).when(requestHeadersSpec).retrieve();
            doReturn(Mono.error(new RuntimeException("API indisponível")))
                    .when(responseSpec).bodyToMono(SearchResponse.class);

            StepVerifier.create(productService.searchAndSave("erro"))
                    .expectErrorMessage("API indisponível")
                    .verify();
        }
    }

    // =======================================================================
    // findAll
    // =======================================================================

    @Nested
    @DisplayName("findAll()")
    class FindAll {

        @Test
        @DisplayName("deve retornar todos os produtos do repositório")
        void shouldReturnAllProducts() {
            Product p1 = buildProduct("MLB1", new BigDecimal("100"), null);
            Product p2 = buildProduct("MLB2", new BigDecimal("200"), null);

            when(repository.findAll()).thenReturn(Flux.just(p1, p2));

            StepVerifier.create(productService.findAll())
                    .expectNext(p1)
                    .expectNext(p2)
                    .verifyComplete();
        }

        @Test
        @DisplayName("deve retornar Flux vazio quando não há produtos")
        void shouldReturnEmptyFlux() {
            when(repository.findAll()).thenReturn(Flux.empty());

            StepVerifier.create(productService.findAll())
                    .verifyComplete();
        }
    }

    // =======================================================================
    // findById
    // =======================================================================

    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("deve retornar o produto quando encontrado")
        void shouldReturnProductWhenFound() {
            Product product = buildProduct("MLB1", new BigDecimal("500"), null);
            when(repository.findById("MLB1")).thenReturn(Mono.just(product));

            StepVerifier.create(productService.findById("MLB1"))
                    .expectNext(product)
                    .verifyComplete();
        }

        @Test
        @DisplayName("deve retornar Mono vazio quando não encontrado")
        void shouldReturnEmptyMonoWhenNotFound() {
            when(repository.findById("INEXISTENTE")).thenReturn(Mono.empty());

            StepVerifier.create(productService.findById("INEXISTENTE"))
                    .verifyComplete();
        }
    }

    // =======================================================================
    // findByCategory
    // =======================================================================

    @Nested
    @DisplayName("findByCategory()")
    class FindByCategory {

        @Test
        @DisplayName("deve retornar produtos da categoria informada")
        void shouldReturnProductsByCategory() {
            Product p = buildProduct("MLB1", new BigDecimal("300"), null);
            when(repository.findByCategoryId("MLB1234")).thenReturn(Flux.just(p));

            StepVerifier.create(productService.findByCategory("MLB1234"))
                    .expectNext(p)
                    .verifyComplete();
        }

        @Test
        @DisplayName("deve retornar Flux vazio para categoria sem produtos")
        void shouldReturnEmptyForUnknownCategory() {
            when(repository.findByCategoryId(anyString())).thenReturn(Flux.empty());

            StepVerifier.create(productService.findByCategory("CAT999"))
                    .verifyComplete();
        }
    }

    // =======================================================================
    // save
    // =======================================================================

    @Nested
    @DisplayName("save()")
    class Save {

        @Test
        @DisplayName("deve atualizar updatedAt e salvar o produto")
        void shouldUpdateTimestampAndSave() {
            Product product = buildProduct("MLB1", new BigDecimal("100"), null);
            product.setUpdatedAt(Instant.EPOCH); // valor "antigo" intencional

            Product saved = buildProduct("MLB1", new BigDecimal("100"), null);
            when(repository.save(any(Product.class))).thenReturn(Mono.just(saved));

            Instant before = Instant.now();

            StepVerifier.create(productService.save(product))
                    .assertNext(p -> assertThat(product.getUpdatedAt()).isAfterOrEqualTo(before))
                    .verifyComplete();

            verify(repository).save(product);
        }
    }

    // =======================================================================
    // delete
    // =======================================================================

    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("deve deletar o produto pelo ID e completar sem emitir item")
        void shouldDeleteById() {
            when(repository.deleteById("MLB1")).thenReturn(Mono.empty());

            StepVerifier.create(productService.delete("MLB1"))
                    .verifyComplete();

            verify(repository).deleteById("MLB1");
        }
    }

    // =======================================================================
    // findPromotions
    // =======================================================================

    @Nested
    @DisplayName("findPromotions()")
    class FindPromotions {

        @Test
        @DisplayName("deve retornar apenas produtos com originalPrice maior que price")
        void shouldReturnOnlyDiscountedProducts() {
            Product withDiscount = buildProduct("MLB1", new BigDecimal("800"), new BigDecimal("1000"));
            Product noDiscount   = buildProduct("MLB2", new BigDecimal("500"), new BigDecimal("500"));
            Product noOriginal   = buildProduct("MLB3", new BigDecimal("300"), null);

            when(repository.findAll()).thenReturn(Flux.just(withDiscount, noDiscount, noOriginal));

            StepVerifier.create(productService.findPromotions())
                    .expectNext(withDiscount)
                    .verifyComplete();
        }

        @Test
        @DisplayName("deve retornar Flux vazio quando nenhum produto tem desconto")
        void shouldReturnEmptyWhenNoPromotions() {
            Product p = buildProduct("MLB1", new BigDecimal("100"), null);
            when(repository.findAll()).thenReturn(Flux.just(p));

            StepVerifier.create(productService.findPromotions())
                    .verifyComplete();
        }

        @Test
        @DisplayName("não deve incluir produto onde originalPrice igual ao price")
        void shouldExcludeProductsWithSamePrice() {
            Product samePrice = buildProduct("MLB4", new BigDecimal("200"), new BigDecimal("200"));
            when(repository.findAll()).thenReturn(Flux.just(samePrice));

            StepVerifier.create(productService.findPromotions())
                    .verifyComplete();
        }

        @Test
        @DisplayName("deve retornar Flux vazio quando repositório está vazio")
        void shouldReturnEmptyWhenRepositoryEmpty() {
            when(repository.findAll()).thenReturn(Flux.empty());

            StepVerifier.create(productService.findPromotions())
                    .verifyComplete();
        }
    }
}