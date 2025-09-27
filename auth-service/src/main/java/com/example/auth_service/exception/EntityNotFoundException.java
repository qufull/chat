package com.example.auth_service.exception;

import org.springframework.http.HttpStatus;

public class EntityNotFoundException extends BaseException {
    public EntityNotFoundException(String message) {
        super(message,HttpStatus.NOT_FOUND);
    }
}
