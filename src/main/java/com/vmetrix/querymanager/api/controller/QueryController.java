package com.vmetrix.querymanager.api.controller;

import com.vmetrix.querymanager.api.dto.request.QueryRequestDto;
import com.vmetrix.querymanager.api.dto.response.QueryBuildResponse;
import com.vmetrix.querymanager.api.dto.response.ValidationErrorDto;
import com.vmetrix.querymanager.api.dto.response.ValidationResponse;
import com.vmetrix.querymanager.api.exception.ErrorResponse;
import com.vmetrix.querymanager.api.mapper.QueryBuildResponseMapper;
import com.vmetrix.querymanager.api.mapper.QueryRequestMapper;
import com.vmetrix.querymanager.api.mapper.ValidationErrorMapper;
import com.vmetrix.querymanager.application.service.QueryService;
import com.vmetrix.querymanager.application.service.QueryValidator;
import com.vmetrix.querymanager.domain.engine.builder.QuerySpecification;
import com.vmetrix.querymanager.domain.model.QueryResult;
import com.vmetrix.querymanager.domain.model.ValidationError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/query")
@RequiredArgsConstructor
@Validated
@Tag(name = "Query API",
        description = "Endpoints that turn a high-level query specification into "
                + "parameterized SQL or validate it without generating SQL.")
public class QueryController {

    private static final String BUILD_REQUEST_EXAMPLE = """
            {
              "select": [
                { "entity": "transaction", "field": "txnDate" },
                { "entity": "counterparty", "field": "partyName", "alias": "counterpartyName" }
              ],
              "filters": {
                "operator": "AND",
                "conditions": [
                  { "entity": "transaction", "field": "status",
                    "comparator": "equals", "value": "SETTLED" }
                ]
              },
              "sorting": [
                { "entity": "transaction", "field": "txnDate", "direction": "DESC" }
              ],
              "maxResults": 500
            }
            """;

    private static final String BUILD_RESPONSE_EXAMPLE = """
            {
              "sql": "SELECT t.TXN_DATE, cp.PARTY_NAME AS counterpartyName FROM TRANSACTION t LEFT JOIN PARTY cp ON t.COUNTERPARTY_ID = cp.PARTY_ID WHERE t.STATUS = :p1 ORDER BY t.TXN_DATE DESC FETCH FIRST 500 ROWS ONLY",
              "parameters": { "p1": "SETTLED" },
              "resolvedTables": ["TRANSACTION", "PARTY"],
              "resolvedJoins": ["LEFT JOIN PARTY cp ON t.COUNTERPARTY_ID = cp.PARTY_ID"],
              "metadata": {
                "columnCount": 2,
                "filterCount": 1,
                "generatedAt": "2026-04-18T00:00:00Z"
              }
            }
            """;

    private static final String VALIDATE_INVALID_RESPONSE_EXAMPLE = """
            {
              "valid": false,
              "errors": [
                {
                  "entity": "transaction",
                  "field": "status",
                  "comparator": "greaterThan",
                  "message": "Comparator 'greaterThan' is not valid for string fields"
                }
              ]
            }
            """;

    private static final String ERROR_RESPONSE_EXAMPLE = """
            {
              "timestamp": "2026-04-18T00:00:00Z",
              "status": 400,
              "error": "Bad Request",
              "message": "Entity unknown_entity does not exist in metadata"
            }
            """;

    private final QueryService queryService;
    private final QueryValidator queryValidator;
    private final QueryRequestMapper queryRequestMapper;
    private final QueryBuildResponseMapper queryBuildResponseMapper;
    private final ValidationErrorMapper validationErrorMapper;

    @PostMapping("/build")
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
    public ResponseEntity<QueryBuildResponse> buildQuery(@Valid @RequestBody QueryRequestDto request) {
        QuerySpecification spec = queryRequestMapper.toDomain(request);
        QueryResult result = queryService.buildQuery(spec);
        return ResponseEntity.ok(queryBuildResponseMapper.toDto(result));
    }

    @PostMapping("/validate")
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
                            examples = @ExampleObject(value = "{\"valid\":true,\"errors\":[]}"))),
            @ApiResponse(responseCode = "400",
                    description = "Specification is invalid; response lists ALL errors found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ValidationResponse.class),
                            examples = @ExampleObject(value = VALIDATE_INVALID_RESPONSE_EXAMPLE)))
    })
    public ResponseEntity<ValidationResponse> validateQuery(@Valid @RequestBody QueryRequestDto request) {
        QuerySpecification spec = queryRequestMapper.toDomain(request);
        List<ValidationError> errors = queryValidator.validate(spec);
        if (errors.isEmpty()) {
            return ResponseEntity.ok(ValidationResponse.builder().valid(true).build());
        }
        List<ValidationErrorDto> errorDtos = validationErrorMapper.toDtoList(errors);
        return ResponseEntity.badRequest()
                .body(ValidationResponse.builder().valid(false).errors(errorDtos).build());
    }
}
