package com.example.authenticationservice.store;

import com.example.authenticationservice.dto.SessionData;
import com.example.authenticationservice.mapper.JsonConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class RefreshTokenStore {
    private final RedisTemplate<String, String> redis;
    @Value("${auth.tokens.refresh-ttl-seconds}") long ttl;

    private final JsonConverter mapper;

    private static final String SESSION_PREFIX = "refresh:session:";
    private static final String USER_INDEX = "user:sessions:";
    private static final String DEVICE_INDEX = "user:device:";

    public void save(SessionData session) {
        String json = mapper.toJson(session);
        redis.opsForValue().set(SESSION_PREFIX + session.getSessionId(), json, Duration.ofSeconds(ttl));
        redis.opsForSet().add(USER_INDEX + session.getUserId(), session.getSessionId());
        redis.opsForValue().set(
                DEVICE_INDEX + session.getUserId() + ":" + session.getDeviceId(),
                session.getSessionId(),
                Duration.ofSeconds(ttl)
        );
    }

    public Optional<SessionData> findByUserAndDevice(String userId, String deviceId) {
        String sessionId = redis.opsForValue().get(DEVICE_INDEX + userId + ":" + deviceId);
        if (sessionId == null) return Optional.empty();

        String json = redis.opsForValue().get(SESSION_PREFIX + sessionId);
        if (json == null) return Optional.empty();

        return Optional.of(mapper.toObject(json,SessionData.class));
    }

    public void deleteSession(String sessionId) {
        get(sessionId).ifPresent(session -> {
            redis.delete(SESSION_PREFIX + sessionId);
            redis.delete(DEVICE_INDEX + session.getUserId() + ":" + session.getDeviceId());
            redis.opsForSet().remove(USER_INDEX + session.getUserId(), sessionId);
        });
    }

    public Optional<SessionData> get(String sessionId) {
        String json = redis.opsForValue().get(SESSION_PREFIX + sessionId);
        if (json == null) return Optional.empty();
        return Optional.of(mapper.toObject(json, SessionData.class));
    }

    public Set<String> getAllForUser(String userId) {
        return redis.opsForSet().members(USER_INDEX + userId);
    }

    public void deleteAllForUser(String userId) {
        Set<String> sessions = redis.opsForSet().members(USER_INDEX + userId);
        if (sessions != null) {
            for (String sid : sessions) {
                deleteSession(sid);
            }
        }
        redis.delete(USER_INDEX + userId);
    }
}
