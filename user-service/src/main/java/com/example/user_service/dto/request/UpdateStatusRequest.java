package com.example.user_service.dto.request;

import com.example.user_service.model.enums.ProfileStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateStatusRequest {
    @NotNull(message = "Status is required")
    ProfileStatus status;
}
