package com.example.authenticationservice.service.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
@Slf4j
@RequiredArgsConstructor
public class DeviceSignatureVerifierHandler {
    private final DeviceSignatureHandler device;
    public boolean verify(String deviceId, String timestamp, String signature) {
        String secret = device.getSecret(deviceId);
        if (secret == null) return false;

        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] result = mac.doFinal((deviceId + ":" + timestamp).getBytes(StandardCharsets.UTF_8));
            String expected = Base64.getUrlEncoder().withoutPadding().encodeToString(result);
            log.info("expected: {}", expected);
            log.info("provided: {}", signature);
            return expected.equals(signature);
        } catch (Exception e) {
            return false;
        }
    }
}
