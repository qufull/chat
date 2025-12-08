package com.example.authenticationservice.service;

import com.example.authenticationservice.dto.SessionData;
import com.example.authenticationservice.exception.UnauthorizedException;
import com.example.authenticationservice.store.RefreshTokenStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogoutService {

    private final RefreshTokenStore store;
    private final TokenService oidc;

    @Transactional
    public void logout(String sessionId){
        SessionData session = store.get(sessionId)
                .orElseThrow(() -> new UnauthorizedException("Session not found"));


        oidc.logout(session.getRefreshToken());

        store.deleteSession(sessionId);

        log.info("Session {} for user {} logged out successfully", sessionId, session.getUserId());
    }

    @Transactional
    public void logoutAll(String userId) {
        Set<String> sessions = store.getAllForUser(userId);
        if (sessions == null || sessions.isEmpty()) {
            log.info("No active sessions for user {}", userId);
            return;
        }

        for (String sid : sessions) {
            store.get(sid).ifPresent(session -> {
                try {
                    oidc.logout(session.getRefreshToken());
                } catch (Exception e) {
                    log.warn("Failed to revoke token for session {}: {}", sid, e.getMessage());
                }
            });
        }

        store.deleteAllForUser(userId);
        log.info("All sessions cleared for user {}", userId);
    }
}
