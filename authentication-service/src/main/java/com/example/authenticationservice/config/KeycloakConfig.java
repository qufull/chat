package com.example.authenticationservice.config;

import lombok.Getter;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class KeycloakConfig {

    @Value("${auth.keycloak.base-url}")
    private String baseUrl;

    @Value("${auth.keycloak.admin.realm}")
    private String adminRealm;

    @Value("${auth.keycloak.admin.client-id}")
    private String adminClientId;

    @Value("${auth.keycloak.admin.username}")
    private String adminUser;

    @Value("${auth.keycloak.admin.password}")
    private String adminPass;

    @Value("${auth.keycloak.realm}")
    private String userRealm;

    @Value("${auth.keycloak.client-id}")
    private String clientId;

    @Value("${auth.keycloak.client-secret}")
    private String clientSecret;

    @Bean
    public Keycloak keycloakAdminClient() {
        return KeycloakBuilder.builder()
                .serverUrl(baseUrl)
                .realm(adminRealm)
                .clientId(adminClientId)
                .grantType(OAuth2Constants.PASSWORD)
                .username(adminUser)
                .password(adminPass)
                .build();
    }

    @Bean public String keycloakRealm() { return userRealm; }
    @Bean public String keycloakClientId() { return clientId; }
    @Bean public String keycloakClientSecret() { return clientSecret; }

}
