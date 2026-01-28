package com.example.user_service.dto.response;

import com.example.user_service.model.enums.ProfileStatus;
import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserProfileResponse {
    private String email;
    private String nickname;
    private String avatarUrl;
    private String bio;
    private ProfileStatus status;
    private String lastSeenAt;
}
