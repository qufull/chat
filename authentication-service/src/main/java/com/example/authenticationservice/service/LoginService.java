package com.example.authenticationservice.service;

import com.example.authenticationservice.dto.LoginRequest;
import com.example.authenticationservice.dto.TokenResponse;
import com.example.authenticationservice.exception.InternalServerException;
import com.example.authenticationservice.exception.KeycloakRequestException;
import com.example.authenticationservice.exception.loginException.InvalidCredentialsException;
import com.example.authenticationservice.store.RefreshTokenStore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class LoginService {

    private final TokenService oidc;
    private final RefreshTokenStore store;
    private final ObjectMapper om = new ObjectMapper();

    public TokenResponse login(LoginRequest req) {
        if (req.username() == null || req.username().isBlank() ||
                req.password() == null || req.password().isBlank()) {
            throw new InvalidCredentialsException();
        }

        Map<String, Object> tokens = oidc.passwordGrant(req.username(), req.password());
        String access = (String) tokens.get("access_token");
        String refresh = (String) tokens.get("refresh_token");
        long expires = ((Number) tokens.getOrDefault("expires_in", 300)).longValue();

        if (access == null || refresh == null) {
            throw new KeycloakRequestException("Missing token fields in Keycloak response");
        }

        String sid = UUID.randomUUID().toString();

        try {
            store.save(sid, om.writeValueAsString(Map.of(
                    "userId", req.username(),
                    "refreshToken", refresh,
                    "createdAt", System.currentTimeMillis()
            )));
        } catch (JsonProcessingException e) {
            throw new InternalServerException("Failed to serialize session data", e);
        }

        return new TokenResponse(access, expires, "Bearer", sid);
    }



}
