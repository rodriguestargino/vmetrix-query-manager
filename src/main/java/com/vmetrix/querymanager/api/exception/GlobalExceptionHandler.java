package com.vmetrix.querymanager.api.exception;

import com.vmetrix.querymanager.shared.exception.InvalidComparatorException;
import com.vmetrix.querymanager.shared.exception.QueryBuildException;
import com.vmetrix.querymanager.shared.exception.UnknownEntityException;
import com.vmetrix.querymanager.shared.exception.UnknownFieldException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UnknownEntityException.class)
    public ResponseEntity<ErrorResponse> handleUnknownEntityException(UnknownEntityException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Unknown entity: '" + ex.getEntityName() + "' is not defined in metadata");
    }

    @ExceptionHandler(UnknownFieldException.class)
    public ResponseEntity<ErrorResponse> handleUnknownFieldException(UnknownFieldException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Unknown field: '" + ex.getFieldName() + "' is not defined for entity '" + ex.getEntityName() + "' in metadata");
    }

    @ExceptionHandler(InvalidComparatorException.class)
    public ResponseEntity<ErrorResponse> handleInvalidComparatorException(InvalidComparatorException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Unknown comparator: '" + ex.getComparator() + "' is not a valid comparator");
    }

    @ExceptionHandler(QueryBuildException.class)
    public ResponseEntity<ErrorResponse> handleQueryBuildException(QueryBuildException ex) {
        log.error("Failed to build query: {}", ex.getMessage());
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + " " + e.getDefaultMessage())
                .collect(Collectors.joining(". ")) + ". ";
        return buildResponse(HttpStatus.BAD_REQUEST, "Validation failed: " + message);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        log.error("Unexpected error", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred. Please contact support.");
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String message) {
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .build();
        return new ResponseEntity<>(response, status);
    }
}
