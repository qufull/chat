package com.example.authenticationservice.exception.registerException;

import com.example.authenticationservice.exception.AppException;
import org.springframework.http.HttpStatus;

public class UsernameAlreadyExistsException extends AppException {
    public UsernameAlreadyExistsException(String username) {
        super("USER_ALREADY_EXISTS", "User with username " + username + " already exists", HttpStatus.CONFLICT);
    }
}
