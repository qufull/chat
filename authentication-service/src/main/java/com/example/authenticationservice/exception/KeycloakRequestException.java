package com.example.authenticationservice.exception;

import org.springframework.http.HttpStatus;

public class KeycloakRequestException extends AppException{

    public KeycloakRequestException(String message) {
        super("KEYCLOAK_REQUEST_FAILED", message, HttpStatus.BAD_GATEWAY);
    }
}
