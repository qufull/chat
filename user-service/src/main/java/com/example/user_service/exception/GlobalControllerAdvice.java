package com.example.user_service.exception;

import com.example.user_service.dto.exception.ErrorResponse;
import com.example.user_service.exception.BaseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.nio.file.AccessDeniedException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
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


    @ExceptionHandler(NicknameAlreadyExistsException.class)
    public ProblemDetail handleNicknameExists(NicknameAlreadyExistsException ex) {
        log.warn("Nickname already exists: {}", ex.getNickname());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                ex.getMessage()
        );
        problem.setTitle("Nickname Already Taken");
        problem.setProperty("nickname", ex.getNickname());
        problem.setProperty("timestamp", Instant.now());

        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.warn("Validation failed: {}", errors);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Validation failed for one or more fields"
        );
        problem.setTitle("Validation Error");
        problem.setProperty("errors", errors);
        problem.setProperty("timestamp", Instant.now());

        return problem;
    }


    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN,
                "You don't have permission to perform this action"
        );
        problem.setTitle("Access Denied");
        problem.setProperty("timestamp", Instant.now());

        return problem;
    }


    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Invalid argument: {}", ex.getMessage());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
        problem.setTitle("Bad Request");
        problem.setProperty("timestamp", Instant.now());

        return problem;
    }
}
