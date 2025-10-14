package com.example.authenticationservice.exception;

import org.springframework.http.HttpStatus;

public class InternalServerException extends AppException{
    public InternalServerException(String message, Throwable cause) {
        super("INTERNAL_ERROR", message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public InternalServerException(String message) {
        super("INTERNAL_ERROR", message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
