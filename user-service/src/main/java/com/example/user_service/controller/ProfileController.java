package com.example.user_service.controller;

import com.example.user_service.dto.request.ProfileRequest;
import com.example.user_service.service.UserProfileService;
import jakarta.ws.rs.Path;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/profile")
public class ProfileController {

    private final UserProfileService userProfileService;

    @GetMapping("/me")
    public String showProfile(){
        return "Profile";
    }



    
}
