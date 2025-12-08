package com.example.authenticationservice.service;

import com.example.authenticationservice.dto.LoginRequest;
import com.example.authenticationservice.dto.OidcTokenResponse;
import com.example.authenticationservice.dto.SessionData;
import com.example.authenticationservice.dto.TokenResponse;
import com.example.authenticationservice.exception.InternalServerException;
import com.example.authenticationservice.exception.KeycloakRequestException;
import com.example.authenticationservice.exception.loginException.InvalidCredentialsException;
import com.example.authenticationservice.mapper.JsonConverter;
import com.example.authenticationservice.service.handler.DeviceSignatureHandler;
import com.example.authenticationservice.store.RefreshTokenStore;
import com.example.authenticationservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class LoginService {

    private final TokenService oidc;
    private final RefreshTokenStore store;
    private final JwtUtil util;
    private final DeviceSignatureHandler deviceSignatureHandler;

    @Transactional
    public TokenResponse login(LoginRequest req) {
        if (req == null || req.getUsername().isBlank() || req.getPassword().isBlank() || req.getDeviceId().isBlank()) {
            throw new InvalidCredentialsException();
        }

        OidcTokenResponse tokens = oidc.passwordGrant(req.getUsername(), req.getPassword());

        log.info(tokens.getRefreshToken());
        log.info(tokens.getAccessToken());

        if (tokens.getAccessToken() == null || tokens.getRefreshToken() == null) {
            throw new KeycloakRequestException("Missing token fields in Keycloak response");
        }


        String userId = util.extractUserId(tokens.getAccessToken());
        String deviceId = req.getDeviceId();

        Optional<SessionData> existing = store.findByUserAndDevice(userId, deviceId);

        if (existing.isPresent()) {
            SessionData session = existing.get();
            return TokenResponse.builder()
                    .accessToken(tokens.getAccessToken())
                    .userId(userId)
                    .sid(session.getSessionId())
                    .deviceId(deviceId)
                    .build();
        }

        String deviceSecret = deviceSignatureHandler.createSecret(deviceId);

        String sid = UUID.randomUUID().toString();
        store.save(SessionData.builder()
                .sessionId(sid)
                .userId(userId)
                .deviceId(deviceId)
                .refreshToken(tokens.getRefreshToken())
                .build()
        );

        return TokenResponse.builder()
                .accessToken(tokens.getAccessToken())
                .sid(sid)
                .userId(userId)
                .deviceId(deviceId)
                .deviceSecret(deviceSecret)
                .build();
    }
}
