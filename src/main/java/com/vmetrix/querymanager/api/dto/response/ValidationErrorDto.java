package com.vmetrix.querymanager.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API-layer representation of a single validation error.
 *
 * <p>Mirrors {@link com.vmetrix.querymanager.domain.model.ValidationError} so that
 * Springdoc / Swagger annotations never leak into the framework-free {@code domain/}
 * layer (see {@code CLAUDE.md} — Strict Rules §4, §5).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A single validation error returned by POST /api/query/validate")
public class ValidationErrorDto {

    @Schema(description = "Logical entity name tied to the error, or null if the error is global",
            example = "transaction")
    private String entity;

    @Schema(description = "Logical field name tied to the error, or null if the error does not target a field",
            example = "status")
    private String field;

    @Schema(description = "Comparator tied to the error, or null if the error is not comparator-related",
            example = "greaterThan")
    private String comparator;

    @Schema(description = "Human-readable explanation of why the condition is invalid",
            example = "Comparator 'greaterThan' is not valid for string fields")
    private String message;
}
