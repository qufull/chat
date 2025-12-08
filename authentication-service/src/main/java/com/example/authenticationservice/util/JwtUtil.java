package com.example.authenticationservice.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    public String extractUserId(String accessToken) {
        try {
            String[] parts = accessToken.split("\\.");
            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));
            Map<String, Object> payload = new ObjectMapper()
                    .readValue(payloadJson, new TypeReference<Map<String, Object>>() {});
            return (String) payload.get("sub");
        } catch (Exception e) {
            log.error("Failed to extract userId from token {}:", e.getMessage());
            throw new RuntimeException(e);
        }
    }

}
