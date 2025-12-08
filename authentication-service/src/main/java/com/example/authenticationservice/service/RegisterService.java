package com.example.authenticationservice.service;

import com.example.authenticationservice.dto.RegisterRequest;
import com.example.authenticationservice.exception.KeycloakRequestException;
import com.example.authenticationservice.exception.registerException.UserAlreadyExistsException;
import com.example.authenticationservice.exception.registerException.UsernameAlreadyExistsException;
import com.example.authenticationservice.producer.UserEventProducer;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import java.util.Collections;

@RequiredArgsConstructor
@Service
public class RegisterService {

    private final Keycloak keycloak;
    private final String keycloakRealm;
    private final UserEventProducer producer;

    public String createUser(RegisterRequest registerRequest) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setEmailVerified(true);
        user.setEnabled(true);
        user.setRequiredActions(Collections.emptyList());
        Response resp = keycloak.realm(keycloakRealm).users().create(user);

        if (resp.getStatus() >= 300) {
            String body = resp.readEntity(String.class);

            if (resp.getStatus() == 409) {
                if (body != null) {
                    if (body.contains("email")) {
                        throw new UserAlreadyExistsException(registerRequest.getEmail());
                    } else if (body.contains("username")) {
                        throw new UsernameAlreadyExistsException(registerRequest.getUsername());
                    }
                }
                throw new UserAlreadyExistsException(registerRequest.getUsername());
            }

            throw new KeycloakRequestException("Keycloak create user failed: " + resp.getStatus() + " | " + body);
        }

        String userId = CreatedResponseUtil.getCreatedId(resp);
        setUserPassword(userId,registerRequest.getPassword(),false);

        producer.publishUserCreated(userId, registerRequest.getEmail(),registerRequest.getUsername());
        return userId;
    }

    public void setUserPassword(String userId, String password, boolean temporary) {
        CredentialRepresentation cred = new CredentialRepresentation();
        cred.setType(CredentialRepresentation.PASSWORD);
        cred.setTemporary(temporary);
        cred.setValue(password);
        keycloak.realm(keycloakRealm).users().get(userId).resetPassword(cred);

    }
}
