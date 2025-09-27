package com.example.auth_service.security.handler;

import com.example.auth_service.model.RefreshToken;
import com.example.auth_service.repository.RefreshTokenRepository;
import com.example.auth_service.service.RefreshTokenService;
import com.example.auth_service.util.CookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import static com.example.auth_service.util.TokenUtil.resolveToken;

@Component
@RequiredArgsConstructor
public class CustomLogoutHandler implements LogoutHandler {

    private final RefreshTokenRepository tokenRepository;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        String token = resolveToken(request);

        if (token != null) {
            RefreshToken tokenEntity = tokenRepository.findByAccessToken(token).orElse(null);

            if (tokenEntity != null) {
                tokenEntity.setLoggedOut(true);
                tokenRepository.save(tokenEntity);
            }
        }
        CookieUtil.clearAuthCookies(response);

        SecurityContextHolder.clearContext();
    }
}
