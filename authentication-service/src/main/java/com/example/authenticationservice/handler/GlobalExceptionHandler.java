package com.example.authenticationservice.handler;

import com.example.authenticationservice.dto.ErrorResponse;
import com.example.authenticationservice.exception.AppException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ErrorResponse handleAppException(AppException ex) {
        return ErrorResponse.builder()
                .error(ex.getCode())
                .message(ex.getMessage())
                .status(ex.getStatus().value())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleOther(Exception ex) {
        return ErrorResponse.builder()
                .error("INTERNAL_ERROR")
                .message(ex.getMessage())
                .status(500)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
