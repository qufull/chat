package com.example.auth_service.repository;

import com.example.auth_service.model.RefreshToken;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    @Query("""
            SELECT t FROM RefreshToken t inner join User u
            on t.user.id = u.id
            where t.user.id = :userId and t.loggedOut = false
            """)
    List<RefreshToken> findAllAccessTokenByUser(UUID userId);

    Optional<RefreshToken> findByAccessToken(String accessToken);

    Optional<RefreshToken> findByRefreshToken(String refreshToken);

}
