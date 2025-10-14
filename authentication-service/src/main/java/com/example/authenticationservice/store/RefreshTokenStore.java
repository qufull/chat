package com.example.authenticationservice.store;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class RefreshTokenStore {
    private final RedisTemplate<String, String> redis;
    @Value("${auth.tokens.refresh-ttl-seconds}") long ttl;

    public void save(String sessionId, String payloadJson) {
        String key = "refresh:" + sessionId;
        redis.opsForValue().set(key, payloadJson, Duration.ofSeconds(ttl));
    }

    public Optional<String> get(String sessionId) {
        String val = redis.opsForValue().get("refresh:" + sessionId);
        return Optional.ofNullable(val);
    }

    public void delete(String sessionId) {
        redis.delete("refresh:" + sessionId);
    }
}
