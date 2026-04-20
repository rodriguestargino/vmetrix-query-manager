package com.vmetrix.querymanager.api.documentation;

import com.vmetrix.querymanager.api.dto.response.EntityMetadataDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static com.vmetrix.querymanager.api.documentation.OpenApiExamples.COMPARATORS_RESPONSE_EXAMPLE;

/**
 * OpenAPI documentation for MetadataController.
 */
public interface MetadataControllerDoc {

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
    ResponseEntity<List<EntityMetadataDto>> getAllEntities();

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
    ResponseEntity<Map<String, List<String>>> getComparators();
}
