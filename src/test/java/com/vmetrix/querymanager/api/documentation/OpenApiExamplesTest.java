package com.vmetrix.querymanager.api.documentation;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OpenApiExamplesTest {

    @Test
    void testExamplesAreLoaded() {
        assertNotNull(OpenApiExamples.BUILD_REQUEST_EXAMPLE, "BUILD_REQUEST_EXAMPLE should not be null");
        assertFalse(OpenApiExamples.BUILD_REQUEST_EXAMPLE.isEmpty(), "BUILD_REQUEST_EXAMPLE should not be empty");
        assertTrue(OpenApiExamples.BUILD_REQUEST_EXAMPLE.contains("\"select\""), "BUILD_REQUEST_EXAMPLE should contain 'select'");

        assertNotNull(OpenApiExamples.BUILD_RESPONSE_EXAMPLE);
        assertTrue(OpenApiExamples.BUILD_RESPONSE_EXAMPLE.contains("\"sql\""));

        assertNotNull(OpenApiExamples.VALIDATE_VALID_RESPONSE_EXAMPLE);
        assertTrue(OpenApiExamples.VALIDATE_VALID_RESPONSE_EXAMPLE.contains("\"valid\": true"));

        assertNotNull(OpenApiExamples.VALIDATE_INVALID_RESPONSE_EXAMPLE);
        assertTrue(OpenApiExamples.VALIDATE_INVALID_RESPONSE_EXAMPLE.contains("\"valid\": false"));

        assertNotNull(OpenApiExamples.EXECUTE_RESPONSE_EXAMPLE);
        assertTrue(OpenApiExamples.EXECUTE_RESPONSE_EXAMPLE.contains("txnId"));

        assertNotNull(OpenApiExamples.ERROR_RESPONSE_EXAMPLE);
        assertTrue(OpenApiExamples.ERROR_RESPONSE_EXAMPLE.contains("\"error\": \"Bad Request\""));

        assertNotNull(OpenApiExamples.COMPARATORS_RESPONSE_EXAMPLE);
        assertTrue(OpenApiExamples.COMPARATORS_RESPONSE_EXAMPLE.contains("\"string\""));
    }
}