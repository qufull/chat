package com.example.user_service.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserSearchResponse {
    private List<UserProfileResponse> users;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;

    public static UserSearchResponse of(List<UserProfileResponse> users,int page, int size, long totalElements, int totalPages) {
        return UserSearchResponse.builder()
                .users(users)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .hasNext(page < totalPages - 1)
                .hasPrevious(page > 0)
                .build();
    }
}
