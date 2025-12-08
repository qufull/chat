package com.example.authenticationservice.service;

import com.example.authenticationservice.dto.OidcTokenResponse;
import com.example.authenticationservice.dto.RefreshRequest;
import com.example.authenticationservice.dto.SessionData;
import com.example.authenticationservice.dto.TokenResponse;
import com.example.authenticationservice.exception.ForbiddenException;
import com.example.authenticationservice.exception.KeycloakRequestException;
import com.example.authenticationservice.exception.UnauthorizedException;
import com.example.authenticationservice.service.handler.DeviceSignatureVerifierHandler;
import com.example.authenticationservice.store.RefreshTokenStore;
import com.example.authenticationservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshService {
    private final TokenService oidc;
    private final RefreshTokenStore store;
    private final DeviceSignatureVerifierHandler verifier;


    public TokenResponse refreshToken(RefreshRequest req,  String deviceId, String timestamp, String signature){
        if (!verifier.verify(deviceId, timestamp, signature)) {
            log.warn("Device signature invalid for {}", deviceId);
            throw new UnauthorizedException("Invalid device signature");
        }

        long ts = Long.parseLong(timestamp);
        long now = System.currentTimeMillis();
        if (Math.abs(now - ts) > 60_000) {
            throw new UnauthorizedException("Request timestamp expired");
        }

        SessionData session = store.get(req.getSid())
                .orElseThrow(() -> new UnauthorizedException("Session not found"));

        if (!session.getDeviceId().equals(deviceId)) {
            throw new ForbiddenException("Device mismatch");
        }

        log.info(session.getRefreshToken());

        OidcTokenResponse tokens = oidc.refreshGrant(session.getRefreshToken());
        if (tokens.getAccessToken() == null || tokens.getRefreshToken() == null) {
            throw new KeycloakRequestException("Keycloak missing token fields");
        }

        session.setRefreshToken(tokens.getRefreshToken());
        store.save(session);

        return TokenResponse.builder()
                .accessToken(tokens.getAccessToken())
                .sid(session.getSessionId())
                .deviceId(deviceId)
                .build();
    }
}
