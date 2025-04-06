package com.example.auth_service.repository;

import com.example.auth_service.model.RefreshToken;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    @EntityGraph(type = EntityGraph.EntityGraphType.FETCH, attributePaths = {"user","user.roles"})
    Optional<RefreshToken> findByToken(String token);
}
