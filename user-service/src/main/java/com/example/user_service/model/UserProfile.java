package com.example.user_service.model;

import com.example.user_service.model.enums.ProfileStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "user_profiles")
public class UserProfile {

    @Id
    private String id;

    @Column(name = "nickname", nullable = false, unique = true, length = 50)
    private String nickname;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "status", length = 50)
    private ProfileStatus status; // "online / offline / custom"

    @Column(name = "about", columnDefinition = "TEXT")
    private String about;

    @Column(name = "last_seen")
    private Instant lastSeen;

}


