package com.example.user_service.dto.request;

import com.example.user_service.model.enums.ProfileStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class ProfileRequest {
    private String nickname;
    private String avatarUrl;
    private ProfileStatus status;
    private String about;
    private Instant lastSeen;
}
