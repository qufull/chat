package com.example.auth_service.service;

import com.example.auth_service.model.Role;
import com.example.auth_service.model.enums.RoleEnum;
import com.example.auth_service.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;

    public Role getRole(RoleEnum roleEnum) {
        return roleRepository.findByName(roleEnum);
    }
}
