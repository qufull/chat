package com.example.user_service.listener;

import com.example.user_service.model.UserProfile;
import com.example.user_service.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RegistrationListener {
    private final UserRepository userRepository;
    private final ObjectMapper mapper;

    @KafkaListener(topics = "keycloak-events", groupId = "user-service")
    public void onUserRegistered(String message) throws Exception {
        JsonNode event = mapper.readTree(message);
        if (!"REGISTER".equals(event.path("type").asText())) return;

        String kcId = event.path("userId").asText();
        String username = event.path("username").asText();

        userRepository.findUserProfilesById(kcId).ifPresentOrElse(
                u -> {}, // уже есть
                () -> {
                    UserProfile profile = UserProfile.builder()
                            .id(kcId)
                            .nickname(username)
                            .build();
                    userRepository.save(profile);
                    System.out.println("✅ Created profile for user " + username);
                });
    }
}
