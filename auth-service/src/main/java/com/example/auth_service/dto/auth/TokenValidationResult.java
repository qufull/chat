package com.example.auth_service.dto.auth;

import com.example.auth_service.model.Role;
import com.example.auth_service.model.enums.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenValidationResult {
    private boolean isValid;
    private String username;
    private UUID userId;
    private List<String> roles;
    private String errorMessage;
}
