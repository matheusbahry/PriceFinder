package com.tcc.price_finder_api.service;

import com.tcc.price_finder_api.dto.security.AuthRequestRecord;
import com.tcc.price_finder_api.enums.auth.UserStatus;
import com.tcc.price_finder_api.model.auth.User;
import com.tcc.price_finder_api.repo.auth.UserRepository;
import com.tcc.price_finder_api.service.security.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    // -----------------------------------------------------------------------
    // Fixtures
    // -----------------------------------------------------------------------

    private static final UUID   USER_ID  = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final String EMAIL    = "john@example.com";
    private static final String PASSWORD = "secret123";

    private User buildUser(UUID id, String email, String password) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setPasswordHash(password);
        user.setStatus(UserStatus.ACTIVE);
        return user;
    }

    private AuthRequestRecord authRequest(String email, String pw) {
        return new AuthRequestRecord(null, null, email, pw);
    }

    // =======================================================================
    // findById()
    // =======================================================================

    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("deve retornar o usuário quando encontrado")
        void shouldReturnUserWhenFound() {
            User user = buildUser(USER_ID, EMAIL, PASSWORD);
            when(userRepository.findById(USER_ID)).thenReturn(Mono.just(user));

            StepVerifier.create(userService.findById(USER_ID))
                    .expectNext(user)
                    .verifyComplete();
        }

        @Test
        @DisplayName("deve retornar Mono vazio quando não encontrado")
        void shouldReturnEmptyWhenNotFound() {
            when(userRepository.findById(USER_ID)).thenReturn(Mono.empty());

            StepVerifier.create(userService.findById(USER_ID))
                    .verifyComplete();
        }
    }

    // =======================================================================
    // findByEmail()
    // =======================================================================

    @Nested
    @DisplayName("findByEmail()")
    class FindByEmail {

        @Test
        @DisplayName("deve retornar o usuário quando e-mail existe")
        void shouldReturnUserWhenEmailExists() {
            User user = buildUser(USER_ID, EMAIL, PASSWORD);
            when(userRepository.findByEmail(EMAIL)).thenReturn(Mono.just(user));

            StepVerifier.create(userService.findByEmail(EMAIL))
                    .expectNext(user)
                    .verifyComplete();
        }

        @Test
        @DisplayName("deve retornar Mono vazio quando e-mail não existe")
        void shouldReturnEmptyWhenEmailNotFound() {
            when(userRepository.findByEmail(EMAIL)).thenReturn(Mono.empty());

            StepVerifier.create(userService.findByEmail(EMAIL))
                    .verifyComplete();
        }
    }

    // =======================================================================
    // existsByEmail()
    // =======================================================================

    @Nested
    @DisplayName("existsByEmail()")
    class ExistsByEmail {

        @Test
        @DisplayName("deve retornar true quando e-mail já está cadastrado")
        void shouldReturnTrueWhenEmailExists() {
            when(userRepository.existsByEmail(EMAIL)).thenReturn(Mono.just(true));

            StepVerifier.create(userService.existsByEmail(EMAIL))
                    .expectNext(true)
                    .verifyComplete();
        }

        @Test
        @DisplayName("deve retornar false quando e-mail não está cadastrado")
        void shouldReturnFalseWhenEmailNotExists() {
            when(userRepository.existsByEmail(EMAIL)).thenReturn(Mono.just(false));

            StepVerifier.create(userService.existsByEmail(EMAIL))
                    .expectNext(false)
                    .verifyComplete();
        }
    }

    // =======================================================================
    // save()
    // =======================================================================

    @Nested
    @DisplayName("save()")
    class Save {

        @Test
        @DisplayName("deve salvar e retornar o usuário")
        void shouldSaveAndReturnUser() {
            User user = buildUser(USER_ID, EMAIL, PASSWORD);
            when(userRepository.save(user)).thenReturn(Mono.just(user));

            StepVerifier.create(userService.save(user))
                    .expectNext(user)
                    .verifyComplete();

            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("deve propagar erro do repositório")
        void shouldPropagateRepositoryError() {
            User user = buildUser(USER_ID, EMAIL, PASSWORD);
            when(userRepository.save(user))
                    .thenReturn(Mono.error(new RuntimeException("DB indisponível")));

            StepVerifier.create(userService.save(user))
                    .expectErrorMessage("DB indisponível")
                    .verify();
        }
    }

    // =======================================================================
    // signUp()
    // =======================================================================

    @Nested
    @DisplayName("signUp()")
    class SignUp {

        @Test
        @DisplayName("deve retornar 204 No Content imediatamente (fire-and-forget)")
        void shouldReturn204NoContent() {
            when(userRepository.save(any(User.class)))
                    .thenReturn(Mono.just(buildUser(null, EMAIL, PASSWORD)));

            StepVerifier.create(userService.sigUp(authRequest(EMAIL, PASSWORD)))
                    .assertNext(response ->
                            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT))
                    .verifyComplete();
        }

        @Test
        @DisplayName("deve construir o novo usuário com e-mail, senha e status ACTIVE")
        void shouldBuildUserWithCorrectFields() {
            when(userRepository.save(any(User.class)))
                    .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

            userService.sigUp(authRequest(EMAIL, PASSWORD)).block();

            verify(userRepository).save(argThat(user ->
                    EMAIL.equals(user.getEmail()) &&
                            PASSWORD.equals(user.getPasswordHash()) &&
                            UserStatus.ACTIVE.equals(user.getStatus()) &&
                            user.getId() == null
            ));
        }
    }

    // =======================================================================
    // login()
    // =======================================================================

    @Nested
    @DisplayName("login()")
    class Login {

        @Test
        @DisplayName("deve retornar 204 No Content quando credenciais são válidas")
        void shouldReturn204WhenCredentialsAreValid() {
            User user = buildUser(USER_ID, EMAIL, PASSWORD);
            when(userRepository.findByEmail(EMAIL)).thenReturn(Mono.just(user));

            StepVerifier.create(userService.login(authRequest(EMAIL, PASSWORD)))
                    .assertNext(response ->
                            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT))
                    .verifyComplete();
        }

        @Test
        @DisplayName("deve retornar 401 Unauthorized quando senha está incorreta")
        void shouldReturn401WhenPasswordIsWrong() {
            User user = buildUser(USER_ID, EMAIL, PASSWORD);
            when(userRepository.findByEmail(EMAIL)).thenReturn(Mono.just(user));

            StepVerifier.create(userService.login(authRequest(EMAIL, "senhaErrada")))
                    .assertNext(response ->
                            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED))
                    .verifyComplete();
        }

        @Test
        @DisplayName("deve retornar 401 Unauthorized quando e-mail não existe")
        void shouldReturn401WhenEmailNotFound() {
            when(userRepository.findByEmail(EMAIL)).thenReturn(Mono.empty());

            StepVerifier.create(userService.login(authRequest(EMAIL, PASSWORD)))
                    .assertNext(response ->
                            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED))
                    .verifyComplete();
        }

        @Test
        @DisplayName("deve propagar erro do repositório")
        void shouldPropagateRepositoryError() {
            when(userRepository.findByEmail(EMAIL))
                    .thenReturn(Mono.error(new RuntimeException("Timeout")));

            StepVerifier.create(userService.login(authRequest(EMAIL, PASSWORD)))
                    .expectErrorMessage("Timeout")
                    .verify();
        }
    }

    // =======================================================================
    // deleteById()
    // =======================================================================

    @Nested
    @DisplayName("deleteById()")
    class DeleteById {

        @Test
        @DisplayName("deve deletar o usuário pelo ID e completar sem emitir item")
        void shouldDeleteAndComplete() {
            when(userRepository.deleteById(USER_ID)).thenReturn(Mono.empty());

            StepVerifier.create(userService.deleteById(USER_ID))
                    .verifyComplete();

            verify(userRepository).deleteById(USER_ID);
        }

        @Test
        @DisplayName("deve propagar erro do repositório")
        void shouldPropagateRepositoryError() {
            when(userRepository.deleteById(USER_ID))
                    .thenReturn(Mono.error(new RuntimeException("Falha ao deletar")));

            StepVerifier.create(userService.deleteById(USER_ID))
                    .expectErrorMessage("Falha ao deletar")
                    .verify();
        }
    }

    // =======================================================================
    // deleteByEmail()
    // =======================================================================

    @Nested
    @DisplayName("deleteByEmail()")
    class DeleteByEmail {

        @Test
        @DisplayName("deve buscar o usuário pelo e-mail e deletar pelo ID")
        void shouldFindByEmailAndDeleteById() {
            User user = buildUser(USER_ID, EMAIL, PASSWORD);
            when(userRepository.findByEmail(EMAIL)).thenReturn(Mono.just(user));
            when(userRepository.deleteById(USER_ID)).thenReturn(Mono.empty());

            StepVerifier.create(userService.deleteByEmail(EMAIL))
                    .verifyComplete();

            verify(userRepository).findByEmail(EMAIL);
            verify(userRepository).deleteById(USER_ID);
        }

        @Test
        @DisplayName("deve completar sem deletar quando e-mail não existe")
        void shouldCompleteWithoutDeletingWhenEmailNotFound() {
            when(userRepository.findByEmail(EMAIL)).thenReturn(Mono.empty());

            StepVerifier.create(userService.deleteByEmail(EMAIL))
                    .verifyComplete();

            verify(userRepository, never()).deleteById(any(UUID.class));
        }

        @Test
        @DisplayName("deve propagar erro do repositório durante findByEmail")
        void shouldPropagateErrorOnFind() {
            when(userRepository.findByEmail(EMAIL))
                    .thenReturn(Mono.error(new RuntimeException("Erro de conexão")));

            StepVerifier.create(userService.deleteByEmail(EMAIL))
                    .expectErrorMessage("Erro de conexão")
                    .verify();
        }

        @Test
        @DisplayName("deve propagar erro do repositório durante deleteById")
        void shouldPropagateErrorOnDelete() {
            User user = buildUser(USER_ID, EMAIL, PASSWORD);
            when(userRepository.findByEmail(EMAIL)).thenReturn(Mono.just(user));
            when(userRepository.deleteById(USER_ID))
                    .thenReturn(Mono.error(new RuntimeException("Falha ao deletar")));

            StepVerifier.create(userService.deleteByEmail(EMAIL))
                    .expectErrorMessage("Falha ao deletar")
                    .verify();
        }
    }
}