package com.example.authenticationservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class UnauthorizedException extends AppException{
    public UnauthorizedException(String message) {
        super("UNAUTHORIZED",message,HttpStatus.UNAUTHORIZED);
    }
}
