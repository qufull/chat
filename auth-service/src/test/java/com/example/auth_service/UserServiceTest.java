package com.example.auth_service;

import com.example.auth_service.model.User;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
public class UserServiceTest {


    @Mock
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Test
    void test_save() {
        User user = User.builder()
                .username("qufull")
                .password("password")
                .email("qufull@example.com")
                .build();

        when(userRepository.save(any(User.class))).thenReturn(user);

        User savedUser = userService.create(user);

        assertThat(savedUser)
                .isNotNull()
                .satisfies(u -> {
                    assertThat(u.getUsername()).isEqualTo("qufull");
                    assertThat(u.getEmail()).isEqualTo("qufull@example.com");
                });

        verify(userRepository).save(any(User.class));
    }
}
