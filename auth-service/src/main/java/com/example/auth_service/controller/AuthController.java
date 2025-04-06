package com.example.auth_service.controller;

import com.example.auth_service.dto.auth.JwtAuthenticationResponse;
import com.example.auth_service.dto.auth.RefreshTokenRequest;
import com.example.auth_service.dto.auth.SignInRequest;
import com.example.auth_service.dto.auth.SignUpRequest;
import com.example.auth_service.dto.auth.SignUpResponse;
import com.example.auth_service.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationService authenticationService;

    @PostMapping("/sign-up")
    public SignUpResponse signUp(@RequestBody @Valid SignUpRequest request) {
        return authenticationService.signUp(request);
    }

    @PostMapping("/sign-in")
    public JwtAuthenticationResponse signIn(@RequestBody @Valid SignInRequest request) {
        return authenticationService.signIn(request);
    }

    @PostMapping("/refreshToken")
    public JwtAuthenticationResponse refreshToken(@RequestBody @Valid RefreshTokenRequest request) {
        return authenticationService.refreshToken(request);
    }
}
