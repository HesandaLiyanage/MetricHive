package com.hess.metrichive.Exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Handles @Valid failures (e.g., missing metric name, empty lists)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {

        // Extract all validation errors and join them into a readable string
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        log.warn("Validation failed: {}", errorMessage);

        ErrorResponse response = ErrorResponse.builder()
                .errorCode("VALIDATION_FAILED")
                .message(errorMessage)
                .timestamp(Instant.now())
                .requestId("req_pending_phase4")
                .build();

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response); // 422 Status
    }
 
    // 2. Handles malformed JSON (e.g., a missing comma in the request body)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMalformedJson(HttpMessageNotReadableException ex) {
        log.warn("Malformed JSON received: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .errorCode("MALFORMED_JSON")
                .message("The request body contains invalid JSON.")
                .timestamp(Instant.now())
                .requestId("req_pending_phase4")
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response); // 400 Status
    }

    // 3. The Catch-All for unexpected server crashes (NullPointerExceptions, Database down, etc.)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllUncaughtException(Exception ex) {
        // We log the full stack trace internally for us...
        log.error("Unknown server error occurred", ex);

        // ...but we hide the details from the user to prevent security leaks
        ErrorResponse response = ErrorResponse.builder()
                .errorCode("INTERNAL_SERVER_ERROR")
                .message("An unexpected error occurred. Our team has been notified.")
                .timestamp(Instant.now())
                .requestId("req_pending_phase4")
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response); // 500 Status
    }
}