package com.example.auth_service.service;

import com.example.auth_service.dto.auth.JwtAuthenticationResponse;
import com.example.auth_service.dto.auth.RefreshTokenRequest;
import com.example.auth_service.dto.auth.SignInRequest;
import com.example.auth_service.dto.auth.SignUpRequest;
import com.example.auth_service.dto.auth.SignUpResponse;
import com.example.auth_service.exception.EntityNotFoundException;
import com.example.auth_service.model.RefreshToken;
import com.example.auth_service.model.User;
import com.example.auth_service.model.enums.RoleEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserService userService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final RoleService roleService;

    @Transactional
    public SignUpResponse signUp(SignUpRequest request) {

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .isActive(true)
                .createdAt(Timestamp.valueOf(LocalDateTime.now()))
                .updatedAt(Timestamp.valueOf(LocalDateTime.now()))
                .roles(Set.of(roleService.getRole(RoleEnum.USER)))
                .build();

        userService.create(user);

        return SignUpResponse.builder()
                .message("Вы успешно зарегестрировались")
                .build();
    }

    public JwtAuthenticationResponse signIn(SignInRequest request) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
        ));

        if(authentication.isAuthenticated()) {

            User user = (User) authentication.getPrincipal();
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

            return new JwtAuthenticationResponse(jwtService.generateToken(user),refreshToken.getToken() );
        } else {
        throw new EntityNotFoundException("invalid user request..!!", HttpStatus.NOT_FOUND);
    }
    }

    public JwtAuthenticationResponse refreshToken(RefreshTokenRequest request) {
        return refreshTokenService.getRefreshToken(request.getRefreshToken())
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String accessToken = jwtService.generateToken(user);
                    return JwtAuthenticationResponse.builder()
                            .accessToken(accessToken)
                            .token(request.getRefreshToken()).build();
                }).orElseThrow(() -> new EntityNotFoundException("refresh token not found..!!", HttpStatus.NOT_FOUND));

    }
}