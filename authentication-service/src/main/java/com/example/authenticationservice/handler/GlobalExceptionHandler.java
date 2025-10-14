package com.example.authenticationservice.handler;

import com.example.authenticationservice.exception.AppException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<Map<String, Object>> handleAppException(AppException ex) {
        Map<String, Object> body = Map.of(
                "error", ex.getCode(),
                "message", ex.getMessage(),
                "status", ex.getStatus().value(),
                "timestamp", Instant.now()
        );
        return ResponseEntity.status(ex.getStatus()).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleOther(Exception ex) {
        Map<String, Object> body = Map.of(
                "error", "INTERNAL_ERROR",
                "message", ex.getMessage(),
                "status", 500,
                "timestamp", Instant.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
