package com.vmetrix.querymanager.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Represents an outgoing relationship (JOIN) to another entity")
public class RelationMetadataDto {
    
    @Schema(description = "Alias of the relationship used in dot-notation queries", example = "counterparty")
    private String alias;
    
    @Schema(description = "Target entity logical name", example = "party")
    private String targetEntity;
    
    @Schema(description = "SQL JOIN type (e.g. LEFT JOIN, INNER JOIN)", example = "LEFT JOIN")
    private String joinType;
    
    @Schema(description = "Source physical column name for the JOIN condition", example = "COUNTERPARTY_ID")
    private String sourceField;
    
    @Schema(description = "Target physical column name for the JOIN condition", example = "PARTY_ID")
    private String targetField;
}
