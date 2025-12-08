package com.example.authenticationservice.service.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class DeviceSignatureHandler {
    private final RedisTemplate<String,String> redis;
    private static final String PREFIX = "device:secret:";

    public String createSecret(String deviceId) {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        String secret = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        redis.opsForValue().set(PREFIX + deviceId, secret);
        return secret;
    }

    public String getSecret(String deviceId) {
        return redis.opsForValue().get(PREFIX + deviceId);
    }
}
