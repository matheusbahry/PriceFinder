package com.tcc.price_finder_api.controller.security;

import com.tcc.price_finder_api.controller.ebay.EbayAuthController;
import com.tcc.price_finder_api.dto.security.*;
import com.tcc.price_finder_api.enums.auth.RoleName;
import com.tcc.price_finder_api.enums.auth.UserStatus;
import com.tcc.price_finder_api.model.auth.User;
import com.tcc.price_finder_api.service.EmailService;
import com.tcc.price_finder_api.service.security.OtpService;
import com.tcc.price_finder_api.service.security.RoleService;
import com.tcc.price_finder_api.service.security.UserRoleService;
import com.tcc.price_finder_api.service.security.UserService;
import com.tcc.price_finder_api.util.JWTUtil;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private JWTUtil jwtUtil;

    private UserService userService;

    private RoleService roleService;

    private UserRoleService userRoleService;

    private OtpService otpService;

    private EmailService emailService;

    private BCryptPasswordEncoder encoder;

    private static final Logger logger =
            LoggerFactory.getLogger(EbayAuthController.class);

    public AuthController(
            JWTUtil jwtUtil,
            UserService userService,
            RoleService roleService,
            UserRoleService userRoleService,
            OtpService otpService,
            EmailService emailService,
            BCryptPasswordEncoder encoder
    ) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.roleService = roleService;
        this.otpService = otpService;
        this.emailService = emailService;
        this.userRoleService = userRoleService;
        this.encoder = encoder;
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<?>> login(
            @Valid @RequestBody AuthRequestRecord authRequest
    ) {

        return userService.findByEmail(authRequest.email())

                .switchIfEmpty(
                        Mono.error(new BadCredentialsException("Invalid credentials"))
                )

                .flatMap(user -> {

                    if (user.getStatus() != UserStatus.ACTIVE) {

                        return Mono.error(
                                new RuntimeException("EMAIL_NOT_VERIFIED")
                        );
                    }

                    if (!encoder.matches(
                            authRequest.pw(),
                            user.getPasswordHash()
                    )) {

                        return Mono.error(
                                new BadCredentialsException(
                                        "Invalid credentials"
                                )
                        );
                    }

                    String code = otpService.generateCode();

                    return otpService.saveCode(user.getId(), code)
                            .then(emailService.sendCode(user.getEmail(), code))
                            .thenReturn(
                                    ResponseEntity.ok("2FA_REQUIRED")
                            );
                });
    }

    @PostMapping("/verify-2fa")
    public Mono<ResponseEntity<AuthResponseRecord>> verify2FA(
            @RequestBody Verify2FARequest request
    ) {

        return userService.findByEmail(request.email())

                .flatMap(user ->

                        otpService.validate(
                                        user.getId(),
                                        request.code()
                                )

                                .flatMap(valid -> {

                                    if (!valid) {
                                        return Mono.error(
                                                new RuntimeException("Invalid code")
                                        );
                                    }

                                    return userRoleService.findByUserId(user.getId())
                                            .flatMap(userRole ->
                                                    roleService.findById(
                                                            userRole.getRoleId()
                                                    )
                                            )
                                            .map(role ->
                                                    "ROLE_" + role.getName()
                                            )
                                            .collectList()

                                            .map(roles -> {

                                                String token =
                                                        jwtUtil.generateToken(
                                                                user.getEmail(),
                                                                user.getId().toString(),
                                                                roles
                                                        );

                                                return ResponseEntity.ok(
                                                        new AuthResponseRecord(
                                                                token,
                                                                roles.get(0),
                                                                UserResponseDTO.from(user)
                                                        )
                                                );
                                            });
                                })
                );
    }

    @GetMapping("/test-token")
    public Mono<String> testToken(@RequestHeader("Authorization") String auth) {
        String token = auth.replace("Bearer ", "");

        return Mono.just("Token válido para usuário: " +
                jwtUtil.getClaimAsString(token, "roles"));
    }


    @PostMapping("/signup")
    public Mono<ResponseEntity<?>> signup(
            @Valid @RequestBody AuthRequestRecord authRequest
    ) {

        logger.info(
                "Signup request received for email: {}",
                authRequest.email()
        );

        return userService.existsByEmail(authRequest.email())

                .flatMap(exists -> {

                    if (exists) {

                        logger.warn(
                                "Email {} já existe",
                                authRequest.email()
                        );

                        return Mono.just(
                                ResponseEntity
                                        .status(HttpStatus.CONFLICT)
                                        .body("EMAIL_ALREADY_EXISTS")
                        );
                    }

                    User user = new User();

                    user.setFirstName(
                            authRequest.firstName()
                    );

                    user.setLastName(
                            authRequest.lastName()
                    );

                    user.setEmail(
                            authRequest.email()
                    );

                    user.setPassword(
                            authRequest.pw()
                    );

                    user.setStatus(
                            UserStatus.INACTIVE
                    );

                    user.setCreatedAt(
                            Instant.now()
                    );

                    return userService.save(user)

                            .flatMap(savedUser -> {

                                String code =
                                        otpService.generateCode();

                                return otpService
                                        .saveCode(
                                                savedUser.getId(),
                                                code
                                        )

                                        .then(
                                                emailService.sendCode(
                                                        savedUser.getEmail(),
                                                        code
                                                )
                                        )

                                        .thenReturn(
                                                ResponseEntity
                                                        .status(HttpStatus.CREATED)
                                                        .body(
                                                                "VERIFY_EMAIL_REQUIRED"
                                                        )
                                        );
                            });
                });
    }

    @PostMapping("/signup-admin")
    public Mono<ResponseEntity<AuthResponseDTO>> signupAdmin(
            @RequestBody AuthRequestRecord authRequest
    ) {

        logger.info(
                "Signup request received for email: {}",
                authRequest.email()
        );

        return userService.existsByEmail(authRequest.email())

                .flatMap(exists -> {

                    if (exists) {

                        logger.warn(
                                "Email {} já existe",
                                authRequest.email()
                        );

                        return Mono.just(
                                ResponseEntity
                                        .status(HttpStatus.CONFLICT)
                                        .build()
                        );
                    }

                    User user = new User();

                    user.setFirstName(
                            authRequest.firstName()
                    );

                    user.setLastName(
                            authRequest.lastName()
                    );

                    user.setEmail(
                            authRequest.email()
                    );

                    user.setPassword(
                            authRequest.pw()
                    );

                    user.setStatus(
                            UserStatus.ACTIVE
                    );

                    user.setCreatedAt(
                            Instant.now()
                    );

                    return userService.save(user)

                            .doOnNext(savedUser ->
                                    logger.info(
                                            "User saved with ID: {}",
                                            savedUser.getId()
                                    )
                            )

                            .flatMap(savedUser ->

                                    roleService.findByName(
                                                    RoleName.ADMIN
                                            )

                                            .doOnNext(role ->
                                                    logger.info(
                                                            "Role found: {} ({})",
                                                            role.getName(),
                                                            role.getId()
                                                    )
                                            )

                                            .flatMap(role ->

                                                    userRoleService.assignRole(
                                                                    savedUser.getId(),
                                                                    role.getId()
                                                            )

                                                            .thenReturn(role)
                                            )

                                            .map(role -> {

                                                List<String> roles =
                                                        List.of(
                                                                "ROLE_" +
                                                                        role.getName()
                                                        );

                                                String token =
                                                        jwtUtil.generateToken(
                                                                savedUser.getEmail(),
                                                                savedUser.getId().toString(),
                                                                roles
                                                        );

                                                return ResponseEntity
                                                        .status(HttpStatus.CREATED)
                                                        .body(
                                                                new AuthResponseDTO(
                                                                        token,
                                                                        role.getName().toString(),
                                                                        UserResponseDTO.from(savedUser)
                                                                )
                                                        );
                                            })
                            );
                });
    }

    @PostMapping("/verify-signup")
    public Mono<ResponseEntity<AuthResponseDTO>> verifySignup(
            @RequestBody Verify2FARequest request
    ) {

        return userService.findByEmail(request.email())

                .flatMap(user ->

                        otpService.validate(
                                        user.getId(),
                                        request.code()
                                )

                                .flatMap(valid -> {

                                    if (!valid) {
                                        return Mono.error(
                                                new RuntimeException(
                                                        "Invalid code"
                                                )
                                        );
                                    }

                                    user.setStatus(UserStatus.ACTIVE);

                                    return userService.save(user)

                                            .flatMap(updatedUser ->

                                                    roleService.findByName(
                                                                    RoleName.OPERADOR
                                                            )

                                                            .flatMap(role ->

                                                                    userRoleService.assignRole(
                                                                                    updatedUser.getId(),
                                                                                    role.getId()
                                                                            )

                                                                            .thenReturn(role)
                                                            )

                                                            .map(role -> {

                                                                List<String> roles =
                                                                        List.of(
                                                                                "ROLE_" +
                                                                                        role.getName()
                                                                        );

                                                                String token =
                                                                        jwtUtil.generateToken(
                                                                                updatedUser.getEmail(),
                                                                                updatedUser.getId().toString(),
                                                                                roles
                                                                        );

                                                                return ResponseEntity.ok(
                                                                        new AuthResponseDTO(
                                                                                token,
                                                                                role.getName().toString(),
                                                                                UserResponseDTO.from(updatedUser)
                                                                        )
                                                                );
                                                            })
                                            );
                                })
                );
    }

    @DeleteMapping("/delete")
    public Mono<ResponseEntity<Void>> deleteAccount(
            Authentication authentication
    ) {

        AuthenticatedUser authenticatedUser =
                (AuthenticatedUser) authentication.getPrincipal();

        UUID userId = authenticatedUser.id();

        return userService.findById(userId)

                .switchIfEmpty(
                        Mono.error(
                                new RuntimeException("USER_NOT_FOUND")
                        )
                )

                .flatMap(user ->

                        userRoleService
                                .deleteByUserId(user.getId())

                                .then(
                                        otpService.deleteByUserId(user.getId())
                                )

                                .then(
                                        userService.deleteById(user.getId())
                                )

                                .then(
                                        Mono.fromSupplier(() -> {

                                            SecurityContextHolder.clearContext();

                                            return ResponseEntity
                                                    .noContent()
                                                    .<Void>build();
                                        })
                                )
                );
    }

    @GetMapping("/protected")
    public Mono<ResponseEntity<String>> protectedEndpoint() {
        return Mono.just(ResponseEntity.ok("You have accessed a protected endpoint!"));
    }
}
