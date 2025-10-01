package com.example.user_service.exception;

import com.example.user_service.exception.BaseException;
import org.springframework.http.HttpStatus;

public class EntityNotFoundException extends BaseException {
    public EntityNotFoundException(String message) {
        super(message,HttpStatus.NOT_FOUND);
    }
}
