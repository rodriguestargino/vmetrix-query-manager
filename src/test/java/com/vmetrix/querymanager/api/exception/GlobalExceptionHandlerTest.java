package com.vmetrix.querymanager.api.exception;

import com.vmetrix.querymanager.shared.exception.InvalidComparatorException;
import com.vmetrix.querymanager.shared.exception.QueryBuildException;
import com.vmetrix.querymanager.shared.exception.UnknownEntityException;
import com.vmetrix.querymanager.shared.exception.UnknownFieldException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void should_handle_unknown_entity_exception() {
        UnknownEntityException ex = new UnknownEntityException("dummy");
        ResponseEntity<ErrorResponse> response = handler.handleUnknownEntityException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Bad Request", response.getBody().getError());
        assertEquals("Unknown entity: 'dummy' is not defined in metadata", response.getBody().getMessage());
    }

    @Test
    void should_handle_unknown_field_exception() {
        UnknownFieldException ex = new UnknownFieldException("test", "dummy");
        ResponseEntity<ErrorResponse> response = handler.handleUnknownFieldException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Bad Request", response.getBody().getError());
        assertEquals("Unknown field: 'dummy' is not defined for entity 'test' in metadata", response.getBody().getMessage());
    }

    @Test
    void should_handle_invalid_comparator_exception() {
        InvalidComparatorException ex = new InvalidComparatorException("dummy");
        ResponseEntity<ErrorResponse> response = handler.handleInvalidComparatorException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Bad Request", response.getBody().getError());
        assertEquals("Unknown comparator: 'dummy' is not a valid comparator", response.getBody().getMessage());
    }

    @Test
    void should_handle_query_build_exception() {
        QueryBuildException ex = new QueryBuildException("Failed to build query");
        ResponseEntity<ErrorResponse> response = handler.handleQueryBuildException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("Internal Server Error", response.getBody().getError());
        assertEquals("Failed to build query", response.getBody().getMessage());
    }

    @Test
    void should_handle_method_argument_not_valid_exception() {
        MethodParameter parameter = mock(MethodParameter.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(Collections.singletonList(
                new FieldError("queryRequest", "select", "must not be empty")
        ));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);
        ResponseEntity<ErrorResponse> response = handler.handleMethodArgumentNotValidException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Bad Request", response.getBody().getError());
        assertEquals("Validation failed: select must not be empty. ", response.getBody().getMessage());
    }
}
