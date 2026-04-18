package com.vmetrix.querymanager.api.exception;

import com.vmetrix.querymanager.shared.exception.InvalidComparatorException;
import com.vmetrix.querymanager.shared.exception.QueryBuildException;
import com.vmetrix.querymanager.shared.exception.UnknownEntityException;
import com.vmetrix.querymanager.shared.exception.UnknownFieldException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UnknownEntityException.class)
    public ResponseEntity<ErrorResponse> handleUnknownEntityException(UnknownEntityException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(UnknownFieldException.class)
    public ResponseEntity<ErrorResponse> handleUnknownFieldException(UnknownFieldException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(InvalidComparatorException.class)
    public ResponseEntity<ErrorResponse> handleInvalidComparatorException(InvalidComparatorException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(QueryBuildException.class)
    public ResponseEntity<ErrorResponse> handleQueryBuildException(QueryBuildException ex) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        StringBuilder errorMessage = new StringBuilder("Validation failed: ");
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errorMessage.append(fieldError.getField())
                    .append(" ")
                    .append(fieldError.getDefaultMessage())
                    .append(". ");
        }
        return buildErrorResponse(HttpStatus.BAD_REQUEST, errorMessage.toString());
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String message) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT))
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .build();
        
        return new ResponseEntity<>(errorResponse, status);
    }
}
