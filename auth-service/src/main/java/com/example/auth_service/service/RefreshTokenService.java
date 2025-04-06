package com.example.auth_service.service;

import com.example.auth_service.exception.EntityNotFoundException;
import com.example.auth_service.model.RefreshToken;
import com.example.auth_service.model.User;
import com.example.auth_service.repository.RefreshTokenRepository;
import com.example.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshToken createRefreshToken(User user){
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiresAt(Timestamp.valueOf(LocalDateTime.now().plusSeconds(60000))) // set expiry of refresh token to 10 minutes - you can configure it application.properties file
                .createdAt(Timestamp.valueOf(LocalDateTime.now()))
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken verifyExpiration(RefreshToken token){
        if(token.getExpiresAt().compareTo(Timestamp.valueOf(LocalDateTime.now()))<0){
            refreshTokenRepository.delete(token);
            throw new RuntimeException(token.getToken() + " Refresh token is expired. Please make a new login..!");
        }
        return token;
    }

    public Optional<RefreshToken> getRefreshToken(String token){
        return refreshTokenRepository.findByToken(token);
    }
}
