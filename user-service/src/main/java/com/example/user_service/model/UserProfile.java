package com.example.user_service.model;

import com.example.user_service.model.enums.ProfileStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.time.LocalDateTime;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private String avatarUrl;


    private String bio;

    private ProfileStatus status;
    private LocalDateTime lastSeenAt;
    private boolean isActive;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private int version;
}


