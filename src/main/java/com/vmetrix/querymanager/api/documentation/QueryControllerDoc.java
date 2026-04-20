package com.vmetrix.querymanager.api.documentation;

import com.vmetrix.querymanager.api.dto.request.QueryRequestDto;
import com.vmetrix.querymanager.api.dto.response.QueryBuildResponse;
import com.vmetrix.querymanager.api.dto.response.ValidationResponse;
import com.vmetrix.querymanager.api.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

import static com.vmetrix.querymanager.api.documentation.OpenApiExamples.*;

/**
 * OpenAPI documentation for QueryController.
 * This interface separates documentation concerns from the implementation.
 */
public interface QueryControllerDoc {

    @Operation(
            summary = "Generate parameterized SQL from a query specification",
            description = "Resolves logical entity/field names to physical SQL names via metadata, "
                    + "computes the shortest JOIN path using BFS, assembles SELECT/FROM/WHERE/ORDER BY, "
                    + "and returns the generated SQL with named bind parameters. "
                    + "Filter values are ALWAYS bound as parameters (:p1, :p2, ...) — never concatenated."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Query specification (columns + optional filters, sorting, maxResults)",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = QueryRequestDto.class),
                    examples = @ExampleObject(
                            name = "specSection5-1",
                            summary = "Spec §5.1 — SETTLED transactions with counterparty name",
                            value = BUILD_REQUEST_EXAMPLE)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "SQL generated successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = QueryBuildResponse.class),
                            examples = @ExampleObject(
                                     name = "specSection5-1Response",
                                     summary = "Generated SQL for spec §5.1 request",
                                     value = BUILD_RESPONSE_EXAMPLE))),
            @ApiResponse(responseCode = "400",
                    description = "Malformed request or unknown entity / field / comparator",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = ERROR_RESPONSE_EXAMPLE))),
            @ApiResponse(responseCode = "500",
                    description = "Unexpected SQL generation failure",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<QueryBuildResponse> buildQuery(@javax.validation.Valid @org.springframework.web.bind.annotation.RequestBody QueryRequestDto request);

    @Operation(
            summary = "Execute a query and return actual data",
            description = "Builds the parameterized SQL from the specification and executes it "
                    + "against the H2 database. Returns a list of maps representing the result set columns."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Query specification (same shape as /build)",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = QueryRequestDto.class),
                    examples = @ExampleObject(
                            name = "executeSample",
                            value = BUILD_REQUEST_EXAMPLE)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Query executed successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = Map.class)),
                            examples = @ExampleObject(name = "default", value = EXECUTE_RESPONSE_EXAMPLE))),
            @ApiResponse(responseCode = "400", description = "Invalid query specification"),
            @ApiResponse(responseCode = "500", description = "Database execution error")
    })
    ResponseEntity<List<Map<String, Object>>> executeQuery(@javax.validation.Valid @org.springframework.web.bind.annotation.RequestBody QueryRequestDto request);

    @Operation(
            summary = "Validate a query specification without generating SQL",
            description = "Checks entity/field existence, field selectability/filterability, "
                    + "comparator compatibility with the field's data type, and value-shape correctness. "
                    + "Unlike fail-fast validators, this endpoint ALWAYS returns the full list of errors "
                    + "so the caller can surface every problem at once."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Query specification to validate (same shape as POST /api/query/build)",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = QueryRequestDto.class),
                    examples = @ExampleObject(
                            name = "validateSample",
                            value = BUILD_REQUEST_EXAMPLE)))
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Specification is valid; response has valid=true and empty errors",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ValidationResponse.class),
                            examples = @ExampleObject(value = VALIDATE_VALID_RESPONSE_EXAMPLE))),
            @ApiResponse(responseCode = "400",
                    description = "Specification is invalid; response lists ALL errors found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ValidationResponse.class),
                            examples = @ExampleObject(value = VALIDATE_INVALID_RESPONSE_EXAMPLE)))
    })
    ResponseEntity<ValidationResponse> validateQuery(@javax.validation.Valid @org.springframework.web.bind.annotation.RequestBody QueryRequestDto request);
}
