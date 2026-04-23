package com.tcc.price_finder_api.model.auth;

import com.tcc.price_finder_api.enums.auth.UserStatus;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table("users")
public class User {

    @Id
    private UUID id;

    private String email;
    private String passwordHash;
    private UserStatus status;

    private Instant createdAt;
    private Instant lastLoginAt;

    public void setPassword(String password) {
        this.passwordHash = new BCryptPasswordEncoder().encode(password);
    }
    
}
