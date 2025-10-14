package com.example.authenticationservice.service;

import com.example.authenticationservice.exception.loginException.InvalidCredentialsException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@RequiredArgsConstructor
@Service
public class TokenService {
    private final WebClient keycloakWebClient;
    private final String keycloakRealm;
    private final String keycloakClientId;
    private final String keycloakClientSecret;

    public Map<String, Object> passwordGrant(String username, String password) {
        return tokenRequest(Map.of(
                "grant_type", "password",
                "username", username,
                "password", password,
                "client_id", keycloakClientId,
                "client_secret", keycloakClientSecret
        ));
    }

    public Map<String, Object> refreshGrant(String refreshToken) {
        return tokenRequest(Map.of(
                "grant_type", "refresh_token",
                "refresh_token", refreshToken,
                "client_id", keycloakClientId,
                "client_secret", keycloakClientSecret
        ));
    }

    private Map<String, Object> tokenRequest(Map<String, String> form) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        form.forEach(body::add);

        return keycloakWebClient.post()
                .uri("/realms/{realm}/protocol/openid-connect/token", keycloakRealm)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(body))
                .retrieve()
                .onStatus(HttpStatusCode::isError, r -> r.bodyToMono(String.class)
                        .flatMap(msg -> Mono.error(new InvalidCredentialsException())))
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();
    }
    }