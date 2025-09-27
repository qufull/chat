package com.example.auth_service.controller;

import com.example.auth_service.dto.auth.JwtAuthenticationResponse;
import com.example.auth_service.dto.auth.RefreshTokenRequest;
import com.example.auth_service.dto.auth.SignInRequest;
import com.example.auth_service.dto.auth.SignUpRequest;
import com.example.auth_service.dto.auth.SignUpResponse;
import com.example.auth_service.dto.auth.TokenValidationResult;
import com.example.auth_service.model.User;
import com.example.auth_service.service.LoginService;
import com.example.auth_service.service.RefreshTokenService;
import com.example.auth_service.service.RegisterService;
import com.example.auth_service.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.ws.rs.core.HttpHeaders;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final RegisterService registerService;
    private final LoginService loginService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/sign-up")
    public SignUpResponse signUp(@RequestBody @Valid SignUpRequest request) {
        return registerService.signUp(request);
    }

    @PostMapping("/sign-in")
    public ResponseEntity<?> signIn(@RequestBody @Valid SignInRequest request, HttpServletResponse response) {
        JwtAuthenticationResponse authResponse = loginService.signIn(request);

        CookieUtil.setAuthCookies(response,authResponse);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/refreshToken")
    public ResponseEntity<JwtAuthenticationResponse> refreshToken(
            @CookieValue("refresh_token") String token,
            HttpServletResponse response) {

        ResponseEntity<JwtAuthenticationResponse> responseEntity = refreshTokenService.refreshToken(token);
        JwtAuthenticationResponse authResponse = responseEntity.getBody();

        if (authResponse == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

       CookieUtil.setAuthCookies(response,authResponse);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal User user){
        return ResponseEntity.ok(Map.of(
                "username", user.getUsername()
        ));
    }

}
