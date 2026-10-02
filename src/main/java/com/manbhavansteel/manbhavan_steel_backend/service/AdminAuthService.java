package com.manbhavansteel.manbhavan_steel_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AdminAuthService {

    private final String adminUsername;
    private final String adminPassword;

    private final Map<String, Instant> tokens = new ConcurrentHashMap<>();

    private static final Duration TOKEN_DURATION =
            Duration.ofHours(8);

    public AdminAuthService(
            @Value("${manbhavan.admin.username}") String adminUsername,
            @Value("${manbhavan.admin.password}") String adminPassword
    ) {
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    public boolean authenticate(String username, String password) {
        return adminUsername.equals(username)
                && adminPassword.equals(password);
    }

    public String createToken() {
        String token = UUID.randomUUID().toString();

        tokens.put(
                token,
                Instant.now().plus(TOKEN_DURATION)
        );

        return token;
    }

    public boolean isValidToken(String token) {

        if (token == null || token.isBlank()) {
            return false;
        }

        Instant expiry = tokens.get(token);

        if (expiry == null) {
            return false;
        }

        if (Instant.now().isAfter(expiry)) {
            tokens.remove(token);
            return false;
        }

        return true;
    }

    public void logout(String token) {
        if (token != null) {
            tokens.remove(token);
        }
    }
}