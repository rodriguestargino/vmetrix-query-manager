package com.vmetrix.querymanager.api.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Uniform error payload returned by GlobalExceptionHandler for any "
        + "handled exception. For validation-specific errors on POST /api/query/validate, see "
        + "ValidationResponse instead.")
public class ErrorResponse {

    @Schema(description = "ISO-8601 UTC timestamp at which the error was produced.",
            example = "2026-04-18T00:00:00Z")
    private String timestamp;

    @Schema(description = "HTTP status code.", example = "400")
    private int status;

    @Schema(description = "HTTP reason phrase.", example = "Bad Request")
    private String error;

    @Schema(description = "Human-readable description of what went wrong.",
            example = "Entity unknown_entity does not exist in metadata")
    private String message;
}
