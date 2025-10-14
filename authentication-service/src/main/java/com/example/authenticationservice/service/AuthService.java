package com.example.authenticationservice.service;

import com.example.authenticationservice.dto.LoginRequest;
import com.example.authenticationservice.dto.RegisterRequest;
import com.example.authenticationservice.dto.TokenResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final RegisterService registerService;
    private final LoginService loginService;
    private final SessionService sessionService;
    private final TokenService tokenService;

    public Map<String, String> register(RegisterRequest req) {
        String userId = registerService.createUser(req);
        return Map.of("userId", userId);
    }

    public TokenResponse login(LoginRequest req) {
        return loginService.login(req);
    }

    public TokenResponse refresh(String sid) {
        String refresh = sessionService.getRefresh(sid);
        Map<String, Object> tokens = tokenService.refreshGrant(refresh);
        String newAccess = (String) tokens.get("access_token");
        String newRefresh = (String) tokens.get("refresh_token");
        long expires = ((Number) tokens.getOrDefault("expires_in", 300)).longValue();

        sessionService.saveSession(sid, "unknown", newRefresh);

        return new TokenResponse(newAccess, expires, "Bearer",sid);
    }

    public void logout(String sid) {
        sessionService.deleteSession(sid);
    }
}
