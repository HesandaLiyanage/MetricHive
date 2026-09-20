package com.hess.metrichive.Exception;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
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

    private String getRequestId() {
        String reqId = MDC.get("request_id");
        return (reqId != null && !reqId.isBlank()) ? reqId : "req_unknown";
    }

    // 1. Handles @Valid failures (e.g., missing metric name, empty lists)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        log.warn("Validation failed: {}", errorMessage);

        ErrorResponse response = ErrorResponse.builder()
                .errorCode("VALIDATION_FAILED")
                .message(errorMessage)
                .timestamp(Instant.now())
                .requestId(getRequestId())
                .build();

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response); // 422 Status
    }

    // 2. Handles resource not found (e.g., tenant or metric not found)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .errorCode("RESOURCE_NOT_FOUND")
                .message(ex.getMessage())
                .timestamp(Instant.now())
                .requestId(getRequestId())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response); // 404 Status
    }

    // 3. Handles duplicate resource (e.g., duplicate email or api key)
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResourceException(DuplicateResourceException ex) {
        log.warn("Duplicate resource conflict: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .errorCode("CONFLICT")
                .message(ex.getMessage())
                .timestamp(Instant.now())
                .requestId(getRequestId())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response); // 409 Status
    }

    // 4. Handles unauthorized access
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(UnauthorizedException ex) {
        log.warn("Unauthorized: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .errorCode("UNAUTHORIZED")
                .message(ex.getMessage())
                .timestamp(Instant.now())
                .requestId(getRequestId())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response); // 401 Status
    }

    // 5. Handles forbidden access
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbiddenException(ForbiddenException ex) {
        log.warn("Forbidden: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .errorCode("FORBIDDEN")
                .message(ex.getMessage())
                .timestamp(Instant.now())
                .requestId(getRequestId())
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response); // 403 Status
    }

    // 6. Handles illegal arguments
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .errorCode("BAD_REQUEST")
                .message(ex.getMessage())
                .timestamp(Instant.now())
                .requestId(getRequestId())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response); // 400 Status
    }

    // 7. Handles malformed JSON (e.g., a missing comma in the request body)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMalformedJson(HttpMessageNotReadableException ex) {
        log.warn("Malformed JSON received: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .errorCode("MALFORMED_JSON")
                .message("The request body contains invalid JSON.")
                .timestamp(Instant.now())
                .requestId(getRequestId())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response); // 400 Status
    }

    // 8. The Catch-All for unexpected server crashes
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllUncaughtException(Exception ex) {
        log.error("Unknown server error occurred", ex);

        ErrorResponse response = ErrorResponse.builder()
                .errorCode("INTERNAL_SERVER_ERROR")
                .message("An unexpected error occurred. Our team has been notified.")
                .timestamp(Instant.now())
                .requestId(getRequestId())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response); // 500 Status
    }
}