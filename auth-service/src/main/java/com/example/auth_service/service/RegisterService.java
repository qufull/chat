package com.example.auth_service.service;

import com.example.auth_service.dto.auth.SignUpRequest;
import com.example.auth_service.dto.auth.SignUpResponse;
import com.example.auth_service.model.User;
import com.example.auth_service.model.enums.RoleEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RegisterService {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
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
}
