package com.tcc.price_finder_api.service.security;

import com.tcc.price_finder_api.model.auth.TwoFactorCode;
import com.tcc.price_finder_api.repo.auth.TwoFactorCodeRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class OtpService {

    private final TwoFactorCodeRepository repository;

    public OtpService(TwoFactorCodeRepository repository) {
        this.repository = repository;
    }

    public String generateCode() {
        return String.valueOf(
                ThreadLocalRandom.current()
                        .nextInt(100000, 999999)
        );
    }

    public Mono<TwoFactorCode> saveCode(
            UUID userId,
            String code
    ) {

        TwoFactorCode otp = new TwoFactorCode();

        otp.setUserId(userId);
        otp.setCode(code);

        // expira em 5 minutos
        otp.setExpiresAt(
                Instant.now().plusSeconds(300)
        );

        otp.setUsed(false);

        return repository.save(otp);
    }

    public Mono<Boolean> validate(
            UUID userId,
            String code
    ) {

        return repository
                .findTopByUserIdOrderByExpiresAtDesc(userId)

                .flatMap(otp -> {

                    if (otp.isUsed()) {
                        return Mono.just(false);
                    }

                    if (otp.getExpiresAt().isBefore(Instant.now())) {
                        return Mono.just(false);
                    }

                    if (!otp.getCode().equals(code)) {
                        return Mono.just(false);
                    }

                    otp.setUsed(true);

                    return repository.save(otp)
                            .thenReturn(true);
                })

                .defaultIfEmpty(false);
    }

    public Mono<Void> deleteByUserId(UUID userId) {
        return repository.deleteByUserId(userId);
    }

}
