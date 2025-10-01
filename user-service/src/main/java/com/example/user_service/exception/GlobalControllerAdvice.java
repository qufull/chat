package com.example.user_service.exception;

import com.example.user_service.dto.exception.ErrorResponse;
import com.example.user_service.exception.BaseException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalControllerAdvice {
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getHttpStatus(),
                ex.getMessage()
        );
        return ResponseEntity.status(ex.getHttpStatus()).body(errorResponse);
    }
}
