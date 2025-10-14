package com.example.authenticationservice.producer;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserEventProducer {
    private final KafkaTemplate<String, String> kafka;
    @Value("${kafka.topics.user-created}") String topic;

    public void publishUserCreated(String userId, String email,String username) {
        String payload = """
      {"type":"user.created","userId":"%s","email":"%s","username":"%s","ts":%d}
      """.formatted(userId, email,username, System.currentTimeMillis());
        kafka.send(topic, userId, payload);
    }
}
