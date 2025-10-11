package com.example.user_service.service;

import com.example.user_service.dto.request.ProfileRequest;
import com.example.user_service.exception.EntityNotFoundException;
import com.example.user_service.model.UserProfile;
import com.example.user_service.model.enums.ProfileStatus;
import com.example.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;


}
