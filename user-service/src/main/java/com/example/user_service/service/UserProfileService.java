package com.example.user_service.service;

import com.example.user_service.dto.request.ProfileRequest;
import com.example.user_service.dto.response.UserProfileResponse;
import com.example.user_service.exception.EntityNotFoundException;
import com.example.user_service.mapper.UserMapper;
import com.example.user_service.model.UserProfile;
import com.example.user_service.model.enums.ProfileStatus;
import com.example.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor

public class UserProfileService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserProfileResponse getUserProfile(UUID id) {
        UserProfile userProfile = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found"));
        return userMapper.toResponse(userProfile);
    }


}

