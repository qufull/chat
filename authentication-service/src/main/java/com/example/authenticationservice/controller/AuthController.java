package com.example.authenticationservice.controller;

import com.example.authenticationservice.dto.*;
import com.example.authenticationservice.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final LoginService loginService;
    private final RefreshService refreshService;
    private final RegisterService registerService;
    private final LogoutService logout;


    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> register(@Valid @RequestBody RegisterRequest req) {
        return Map.of("userId",registerService.createUser(req));
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public TokenResponse login(@Valid @RequestBody LoginRequest req) {
        return loginService.login(req);
    }

    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public TokenResponse refresh(
            @RequestBody RefreshRequest req,
            @ModelAttribute DeviceHeaders headers,
            @RequestHeader(value = "X-Device-Id", required = false) String fallbackDeviceId,
            @RequestHeader(value = "X-Timestamp", required = false) String fallbackTs,
            @RequestHeader(value = "X-Signature", required = false) String fallbackSig
    ) {

        if (headers.getDeviceId() == null) headers.setDeviceId(fallbackDeviceId);
        if (headers.getTimestamp() == null) headers.setTimestamp(fallbackTs);
        if (headers.getSignature() == null) headers.setSignature(fallbackSig);

        return refreshService.refreshToken(
                req,
                headers.getDeviceId(),
                headers.getTimestamp(),
                headers.getSignature()
        );
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void logout(@RequestHeader("X-Session-Id") String sessionId) {
        logout.logout(sessionId);
    }

    @PostMapping("/logout/all")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void logoutAll(@RequestHeader("X-User-Id") String userId) {
        logout.logoutAll(userId);
    }
}

