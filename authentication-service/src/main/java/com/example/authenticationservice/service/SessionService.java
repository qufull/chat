package com.example.authenticationservice.service;

import com.example.authenticationservice.dto.SessionData;
import com.example.authenticationservice.mapper.JsonConverter;
import com.example.authenticationservice.store.RefreshTokenStore;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SessionService {
    private final RefreshTokenStore store;
    private final JsonConverter mapper;
    Set<RefreshService> a = new HashSet<>();






    public void saveSession(SessionData session) {
        store.save(session);

    }

    public String getRefresh(String userId,String deviceId) {
        return getSession(userId,deviceId)
                .map(SessionData::getRefreshToken)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No session"));
    }

    public void deleteSession(String sid) {
        store.deleteSession(sid);
    }

    public void deleteAllForUser(String userId) {
        store.deleteAllForUser(userId);
    }

    private Optional<SessionData> getSession(String userId,String deviceId) {
        return store.findByUserAndDevice(userId,deviceId);
    }
}
