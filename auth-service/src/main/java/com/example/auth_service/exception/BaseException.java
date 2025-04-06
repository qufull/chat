package com.example.auth_service.exception;

import org.springframework.http.HttpStatus;

public abstract class BaseException extends RuntimeException {
    private final HttpStatus httpStatus;

    public BaseException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
