package com.example.authenticationservice.exception.registerException;

import com.example.authenticationservice.exception.AppException;
import org.springframework.http.HttpStatus;

public class UserAlreadyExistsException extends AppException {
    public UserAlreadyExistsException(String email) {
        super("USER_ALREADY_EXISTS", "User with email " + email + " already exists", HttpStatus.CONFLICT);
    }
}
