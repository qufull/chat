package com.example.user_service.repository;

import com.example.user_service.model.UserProfile;
import org.apache.catalina.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserProfile, UUID> {

    @NativeQuery("select p.nickname, p.avatar_url, p.status, p.last_online, p.last_online FROM user_profiles p JOIN users ON p.user_id = users.id")
    Optional<UserProfile> findUserProfilesById(UUID id);
}
