package com.vmetrix.querymanager.api.controller;

import com.vmetrix.querymanager.api.dto.response.EntityMetadataDto;
import com.vmetrix.querymanager.api.mapper.EntityMetadataMapper;
import com.vmetrix.querymanager.application.service.MetadataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/metadata")
@RequiredArgsConstructor
@Tag(name = "Metadata API",
        description = "Endpoints for discovering the query engine data model — entities, fields, "
                + "relationships, and valid comparators per data type.")
public class MetadataController {

    private static final String COMPARATORS_RESPONSE_EXAMPLE = """
            {
              "string":    ["equals","notEquals","like","in","notIn","isNull","isNotNull"],
              "number":    ["equals","notEquals","greaterThan","lessThan","greaterOrEqual","lessOrEqual","between","in","isNull","isNotNull"],
              "date":      ["equals","notEquals","greaterThan","lessThan","greaterOrEqual","lessOrEqual","between","isNull","isNotNull"],
              "timestamp": ["equals","notEquals","greaterThan","lessThan","greaterOrEqual","lessOrEqual","isNull","isNotNull"]
            }
            """;

    private final MetadataService metadataService;
    private final EntityMetadataMapper mapper;

    @GetMapping("/entities")
    @Operation(summary = "Get all queryable entities",
            description = "Returns a tree of all entities, their fields, and outgoing relationships. "
                    + "Useful for dynamically building query-builder UIs without hardcoding the model.")
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Full metadata tree",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = EntityMetadataDto.class))))
    })
    public ResponseEntity<List<EntityMetadataDto>> getAllEntities() {
        return ResponseEntity.ok(mapper.toDtoList(metadataService.getAllEntities()));
    }

    @GetMapping("/comparators")
    @Operation(summary = "Get valid comparators",
            description = "Returns valid SQL comparators grouped by field data type. Use this to drive "
                    + "context-aware comparator pickers in a query-builder UI.")
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Map of data-type name to list of valid comparator names",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            examples = @ExampleObject(value = COMPARATORS_RESPONSE_EXAMPLE)))
    })
    public ResponseEntity<Map<String, List<String>>> getComparators() {
        return ResponseEntity.ok(metadataService.getComparators());
    }
}
