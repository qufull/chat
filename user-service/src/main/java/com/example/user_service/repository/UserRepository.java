package com.example.user_service.repository;

import com.example.user_service.model.UserProfile;
import com.example.user_service.model.enums.ProfileStatus;
import org.apache.catalina.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserProfile, UUID> {

    Optional<UserProfile> findByIdAndIsActiveTrue(String id);

    Optional<UserProfile> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByNicknameIgnoreCase(String nickname);


    @Query("""
        SELECT u FROM UserProfile u
        WHERE LOWER(u.nickname) LIKE LOWER(CONCAT('%', :query, '%'))
        AND u.isActive = true
        ORDER BY u.nickname
        """)
    Page<UserProfile> searchByNickname(@Param("query") String query, Pageable pageable);


    @Query("SELECT u FROM UserProfile u WHERE u.id IN :ids AND u.isActive = true")
    List<UserProfile> findAllByIdIn(@Param("ids") List<String> ids);


    List<UserProfile> findAllByStatusAndIsActiveTrue(ProfileStatus status);


    @Modifying
    @Query("""
        UPDATE UserProfile u
        SET u.status = 'OFFLINE', u.updatedAt = CURRENT_TIMESTAMP
        WHERE u.status IN ('ONLINE', 'AWAY')
        AND u.lastSeenAt < :threshold
        AND u.isActive = true
        """)
    int markInactiveUsersOffline(@Param("threshold") Instant threshold);

    @Modifying
    @Query("""
        UPDATE UserProfile u
        SET u.lastSeenAt = :timestamp, 
            u.status = :status,
            u.updatedAt = CURRENT_TIMESTAMP
        WHERE u.id = :userId
        """)
    int updateLastSeen(
            @Param("userId") String userId,
            @Param("timestamp") Instant timestamp,
            @Param("status") ProfileStatus status
    );
}
