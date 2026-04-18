package com.vmetrix.querymanager.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Represents a queryable entity in the metadata data dictionary")
public class EntityMetadataDto {

    @Schema(description = "Logical name (used in API paths and payloads)", example = "transaction")
    private String entity;

    @Schema(description = "Physical database table name", example = "TRANSACTION")
    private String physicalTable;

    @Schema(description = "Default SQL alias used in engine generation", example = "t")
    private String alias;

    @Schema(description = "List of fields belonging to this entity")
    private List<FieldMetadataDto> fields;

    @Schema(description = "List of outgoing relationships to other entities")
    private List<RelationMetadataDto> relations;
}
