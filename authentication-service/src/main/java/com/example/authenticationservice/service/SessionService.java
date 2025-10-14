package com.example.authenticationservice.service;

import com.example.authenticationservice.store.RefreshTokenStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class SessionService {
    private final RefreshTokenStore store;
    private final ObjectMapper mapper = new ObjectMapper();

    public void saveSession(String sid, String userId, String refreshToken) {
        try {
            String data = mapper.writeValueAsString(Map.of(
                    "userId", userId,
                    "refreshToken", refreshToken,
                    "createdAt", System.currentTimeMillis()
            ));
            store.save(sid, data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getRefresh(String sid) {
        return store.get(sid)
                .map(json -> {
                    try {
                        Map<String, Object> map = mapper.readValue(json, Map.class);
                        return (String) map.get("refreshToken");
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No session"));
    }

    public void deleteSession(String sid) {
        store.delete(sid);
    }
}
