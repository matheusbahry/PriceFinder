package com.tcc.price_finder_api.service.security;

import com.tcc.price_finder_api.controller.ebay.EbayProductController;
import com.tcc.price_finder_api.model.auth.UserRole;
import com.tcc.price_finder_api.repo.auth.UserRoleRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@Service
public class UserRoleService {

    private final UserRoleRepository userRoleRepository;

    private static final Logger logger =
            LoggerFactory.getLogger(EbayProductController.class);

    public UserRoleService(UserRoleRepository userRoleRepository) {
        this.userRoleRepository = userRoleRepository;
    }

    public Mono<UserRole> assignRole(
            UUID userId,
            UUID roleId
    ) {

        logger.info(
                "Assigning role {} to user {}",
                roleId,
                userId
        );

        return userRoleRepository
                .findByUserIdAndRoleId(userId, roleId)

                .hasElements()

                .flatMap(exists -> {

                    if (exists) {

                        logger.info(
                                "User {} already has role {}",
                                userId,
                                roleId
                        );

                        return Mono.empty();
                    }

                    UserRole userRole = new UserRole();

                    userRole.setUserId(userId);

                    userRole.setRoleId(roleId);

                    logger.info(
                            "Saving UserRole for user {} with role {}",
                            userId,
                            roleId
                    );

                    return userRoleRepository
                            .save(userRole)

                            .doOnNext(ur ->
                                    logger.info(
                                            "UserRole saved for user {} with role {}",
                                            ur.getUserId(),
                                            ur.getRoleId()
                                    )
                            )

                            .doOnError(err ->
                                    logger.error(
                                            "Erro ao salvar UserRole",
                                            err
                                    )
                            );
                });
    }

    public Flux<UserRole> findByUserId(UUID userId) {
        return userRoleRepository.findByUserId(userId);
    }

    public Flux<UserRole> findByRoleId(UUID roleId) {
        return userRoleRepository.findByRoleId(roleId);
    }
    public Mono<Boolean> hasRole(UUID userId, UUID roleId) {

        return userRoleRepository
                .findByUserIdAndRoleId(userId, roleId)
                .hasElements();
    }
    public Mono<Void> deleteByUserId(UUID userId) {
        return userRoleRepository.deleteByUserId(userId);
    }
}

