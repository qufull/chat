package com.example.authenticationservice.controller;

import com.example.authenticationservice.dto.LoginRequest;
import com.example.authenticationservice.dto.RegisterRequest;
import com.example.authenticationservice.dto.TokenResponse;
import com.example.authenticationservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    @Value("${auth.cookie.name}") String cookieName;

    private ResponseCookie sessionCookie(String sid) {
        return ResponseCookie.from(cookieName, sid)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofDays(14))
                .build();
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> register(@Valid @RequestBody RegisterRequest req) {
        return authService.register(req);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest req) {
        TokenResponse token = authService.login(req);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, sessionCookie(token.sid()).toString())
                .body(token);
    }

    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public TokenResponse refresh(@CookieValue(name = "${auth.cookie.name}") String sid) {
        return authService.refresh(sid);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(
            @CookieValue(name = "${auth.cookie.name}", required = false) String sid) {
        authService.logout(sid);
        ResponseCookie expired = ResponseCookie.from(cookieName, "")
                .path("/")
                .maxAge(Duration.ZERO)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, expired.toString())
                .body(Map.of("ok", true));
    }
}

