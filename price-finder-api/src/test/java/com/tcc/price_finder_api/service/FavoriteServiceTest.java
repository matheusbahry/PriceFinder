package com.tcc.price_finder_api.service;

import com.tcc.price_finder_api.model.Favorite;
import com.tcc.price_finder_api.repo.FavoriteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FavoriteService")
class FavoriteServiceTest {

    @Mock
    private FavoriteRepository favoriteRepository;

    @InjectMocks
    private FavoriteService favoriteService;

    // -----------------------------------------------------------------------
    // Fixtures
    // -----------------------------------------------------------------------

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final String PRODUCT_NAME = "Notebook Dell";
    private static final String PRODUCT_URL  = "https://mercadolivre.com/notebook-dell";

    private Favorite buildFavorite(UUID userId, String name, String url) {
        return Favorite.builder()
                .userId(userId)
                .productName(name)
                .productUrl(url)
                .createdAt(Instant.now())
                .build();
    }

    // =======================================================================
    // save()
    // =======================================================================

    @Nested
    @DisplayName("save()")
    class Save {

        @Test
        @DisplayName("deve salvar e retornar o favorito quando produto ainda não está favoritado")
        void shouldSaveWhenNotAlreadyFavorited() {
            Favorite saved = buildFavorite(USER_ID, PRODUCT_NAME, PRODUCT_URL);

            when(favoriteRepository.findByUserIdAndProductUrl(USER_ID, PRODUCT_URL))
                    .thenReturn(Mono.empty());
            when(favoriteRepository.save(any(Favorite.class)))
                    .thenReturn(Mono.just(saved));

            StepVerifier.create(favoriteService.save(USER_ID, PRODUCT_NAME, PRODUCT_URL))
                    .assertNext(f -> {
                        assertThat(f.getUserId()).isEqualTo(USER_ID);
                        assertThat(f.getProductName()).isEqualTo(PRODUCT_NAME);
                        assertThat(f.getProductUrl()).isEqualTo(PRODUCT_URL);
                        assertThat(f.getCreatedAt()).isNotNull();
                    })
                    .verifyComplete();

            verify(favoriteRepository).save(any(Favorite.class));
        }

        @Test
        @DisplayName("deve emitir RuntimeException quando produto já está nos favoritos")
        void shouldErrorWhenAlreadyFavorited() {
            Favorite existing = buildFavorite(USER_ID, PRODUCT_NAME, PRODUCT_URL);

            when(favoriteRepository.findByUserIdAndProductUrl(USER_ID, PRODUCT_URL))
                    .thenReturn(Mono.just(existing));

            StepVerifier.create(favoriteService.save(USER_ID, PRODUCT_NAME, PRODUCT_URL))
                    .expectErrorSatisfies(ex -> {
                        assertThat(ex).isInstanceOf(RuntimeException.class);
                        assertThat(ex.getMessage()).isEqualTo("Produto já está nos favoritos");
                    })
                    .verify();

            verify(favoriteRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve propagar erro do repositório durante save")
        void shouldPropagateRepositoryErrorOnSave() {
            when(favoriteRepository.findByUserIdAndProductUrl(USER_ID, PRODUCT_URL))
                    .thenReturn(Mono.empty());
            when(favoriteRepository.save(any(Favorite.class)))
                    .thenReturn(Mono.error(new RuntimeException("DB indisponível")));

            StepVerifier.create(favoriteService.save(USER_ID, PRODUCT_NAME, PRODUCT_URL))
                    .expectErrorMessage("DB indisponível")
                    .verify();
        }

        @Test
        @DisplayName("deve construir o favorito com createdAt preenchido automaticamente")
        void shouldSetCreatedAtOnNewFavorite() {
            Instant before = Instant.now();

            when(favoriteRepository.findByUserIdAndProductUrl(USER_ID, PRODUCT_URL))
                    .thenReturn(Mono.empty());
            when(favoriteRepository.save(any(Favorite.class)))
                    .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

            StepVerifier.create(favoriteService.save(USER_ID, PRODUCT_NAME, PRODUCT_URL))
                    .assertNext(f -> assertThat(f.getCreatedAt()).isAfterOrEqualTo(before))
                    .verifyComplete();
        }
    }

    // =======================================================================
    // findByUserId()
    // =======================================================================

    @Nested
    @DisplayName("findByUserId()")
    class FindByUserId {

        @Test
        @DisplayName("deve retornar todos os favoritos do usuário")
        void shouldReturnAllFavoritesForUser() {
            Favorite f1 = buildFavorite(USER_ID, "Produto A", "https://url-a.com");
            Favorite f2 = buildFavorite(USER_ID, "Produto B", "https://url-b.com");

            when(favoriteRepository.findAllByUserId(USER_ID)).thenReturn(Flux.just(f1, f2));

            StepVerifier.create(favoriteService.findByUserId(USER_ID))
                    .expectNext(f1)
                    .expectNext(f2)
                    .verifyComplete();
        }

        @Test
        @DisplayName("deve retornar Flux vazio quando usuário não tem favoritos")
        void shouldReturnEmptyFluxWhenNoFavorites() {
            when(favoriteRepository.findAllByUserId(USER_ID)).thenReturn(Flux.empty());

            StepVerifier.create(favoriteService.findByUserId(USER_ID))
                    .verifyComplete();
        }

        @Test
        @DisplayName("deve propagar erro do repositório")
        void shouldPropagateRepositoryError() {
            when(favoriteRepository.findAllByUserId(USER_ID))
                    .thenReturn(Flux.error(new RuntimeException("Falha de conexão")));

            StepVerifier.create(favoriteService.findByUserId(USER_ID))
                    .expectErrorMessage("Falha de conexão")
                    .verify();
        }
    }

    // =======================================================================
    // remove()
    // =======================================================================

    @Nested
    @DisplayName("remove()")
    class Remove {

        @Test
        @DisplayName("deve deletar o favorito quando encontrado e completar sem emitir item")
        void shouldDeleteWhenFound() {
            Favorite existing = buildFavorite(USER_ID, PRODUCT_NAME, PRODUCT_URL);

            when(favoriteRepository.findByUserIdAndProductUrl(USER_ID, PRODUCT_URL))
                    .thenReturn(Mono.just(existing));
            when(favoriteRepository.delete(existing))
                    .thenReturn(Mono.empty());

            StepVerifier.create(favoriteService.remove(USER_ID, PRODUCT_URL))
                    .verifyComplete();

            verify(favoriteRepository).delete(existing);
        }

        @Test
        @DisplayName("deve completar sem deletar quando favorito não é encontrado")
        void shouldCompleteWithoutDeletingWhenNotFound() {
            when(favoriteRepository.findByUserIdAndProductUrl(USER_ID, PRODUCT_URL))
                    .thenReturn(Mono.empty());

            StepVerifier.create(favoriteService.remove(USER_ID, PRODUCT_URL))
                    .verifyComplete();

            verify(favoriteRepository, never()).delete(any());
        }

        @Test
        @DisplayName("deve propagar erro do repositório durante delete")
        void shouldPropagateRepositoryErrorOnDelete() {
            Favorite existing = buildFavorite(USER_ID, PRODUCT_NAME, PRODUCT_URL);

            when(favoriteRepository.findByUserIdAndProductUrl(USER_ID, PRODUCT_URL))
                    .thenReturn(Mono.just(existing));
            when(favoriteRepository.delete(existing))
                    .thenReturn(Mono.error(new RuntimeException("Erro ao deletar")));

            StepVerifier.create(favoriteService.remove(USER_ID, PRODUCT_URL))
                    .expectErrorMessage("Erro ao deletar")
                    .verify();
        }
    }

    // =======================================================================
    // isFavorite()
    // =======================================================================

    @Nested
    @DisplayName("isFavorite()")
    class IsFavorite {

        @Test
        @DisplayName("deve retornar true quando produto está nos favoritos")
        void shouldReturnTrueWhenFavorited() {
            Favorite existing = buildFavorite(USER_ID, PRODUCT_NAME, PRODUCT_URL);

            when(favoriteRepository.findByUserIdAndProductUrl(USER_ID, PRODUCT_URL))
                    .thenReturn(Mono.just(existing));

            StepVerifier.create(favoriteService.isFavorite(USER_ID, PRODUCT_URL))
                    .expectNext(true)
                    .verifyComplete();
        }

        @Test
        @DisplayName("deve retornar false quando produto não está nos favoritos")
        void shouldReturnFalseWhenNotFavorited() {
            when(favoriteRepository.findByUserIdAndProductUrl(USER_ID, PRODUCT_URL))
                    .thenReturn(Mono.empty());

            StepVerifier.create(favoriteService.isFavorite(USER_ID, PRODUCT_URL))
                    .expectNext(false)
                    .verifyComplete();
        }

        @Test
        @DisplayName("deve propagar erro do repositório")
        void shouldPropagateRepositoryError() {
            when(favoriteRepository.findByUserIdAndProductUrl(USER_ID, PRODUCT_URL))
                    .thenReturn(Mono.error(new RuntimeException("Timeout")));

            StepVerifier.create(favoriteService.isFavorite(USER_ID, PRODUCT_URL))
                    .expectErrorMessage("Timeout")
                    .verify();
        }
    }
}