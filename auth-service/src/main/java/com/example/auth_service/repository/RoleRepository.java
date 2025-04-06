package com.example.auth_service.repository;

import com.example.auth_service.model.Role;
import com.example.auth_service.model.enums.RoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    Role findByName(RoleEnum name);

}
