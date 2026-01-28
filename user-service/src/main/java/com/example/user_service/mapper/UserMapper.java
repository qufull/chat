package com.example.user_service.mapper;


import com.example.user_service.dto.response.UserProfileResponse;
import com.example.user_service.model.UserProfile;
import com.example.user_service.model.enums.ProfileStatus;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserProfileResponse toResponse(UserProfile entity);

    List<UserProfileResponse> toResponseList(List<UserProfile> entities);

    @AfterMapping
    default void addLastSeenFormatted(UserProfile entity, @MappingTarget UserProfileResponse.UserProfileResponseBuilder builder) {
        if (entity.getStatus() == ProfileStatus.ONLINE) {
            builder.status(ProfileStatus.ONLINE);
        } else if (entity.getLastSeenAt() != null) {
            builder.lastSeenAt(formatLastSeen(entity.getLastSeenAt()));
        }
    }

    private String formatLastSeen(LocalDateTime lastSeen) {
        Duration duration = Duration.between(lastSeen, Instant.now());

        long seconds = duration.getSeconds();

        if (seconds < 60) {
            return "Just now";
        }

        long minutes = seconds / 60;
        if (minutes < 60) {
            return minutes == 1 ? "1 minute ago" : minutes + " minutes ago";
        }

        long hours = minutes / 60;
        if (hours < 24) {
            return hours == 1 ? "1 hour ago" : hours + " hours ago";
        }

        long days = hours / 24;
        if (days == 1) {
            return "Yesterday";
        }
        if (days < 7) {
            return days + " days ago";
        }

        long weeks = days / 7;
        if (weeks < 4) {
            return weeks == 1 ? "1 week ago" : weeks + " weeks ago";
        }

        long months = days / 30;
        if (months < 12) {
            return months == 1 ? "1 month ago" : months + " months ago";
        }

        long years = days / 365;
        return years == 1 ? "1 year ago" : years + " years ago";
    }
}
