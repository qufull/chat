package com.example.authenticationservice.exception.loginException;

import com.example.authenticationservice.exception.AppException;
import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends AppException {
    public InvalidCredentialsException() {
        super("INVALID_CREDENTIALS", "Invalid username or password", HttpStatus.UNAUTHORIZED);
    }
}
