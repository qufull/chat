package com.example.auth_service.service;

import com.example.auth_service.dto.auth.JwtAuthenticationResponse;
import com.example.auth_service.dto.auth.SignInRequest;
import com.example.auth_service.exception.EntityNotFoundException;
import com.example.auth_service.model.RefreshToken;
import com.example.auth_service.model.User;
import com.example.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginService {
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;

    public JwtAuthenticationResponse signIn(SignInRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );


        User user = (User) authentication.getPrincipal();

        if(user.getUsername().equals(request.getUsername())){

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        refreshTokenService.revokeAllToken(user);
        refreshTokenService.saveUserToken(accessToken,refreshToken,user);

            return new JwtAuthenticationResponse(accessToken,refreshToken);
        } else {
            throw new EntityNotFoundException("invalid user request..!!");
        }
    }
}
