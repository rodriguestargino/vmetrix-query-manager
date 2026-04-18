package com.vmetrix.querymanager.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Represents a single field within a metadata entity")
public class FieldMetadataDto {
    
    @Schema(description = "Logical name (camelCase)", example = "txnDate")
    private String name;
    
    @Schema(description = "Physical database column name", example = "TXN_DATE")
    private String physicalName;
    
    @Schema(description = "Data type (string, number, date, timestamp)", example = "date")
    private String type;
    
    @Schema(description = "Indicates if this field is part of the primary key", example = "false")
    private boolean primaryKey;
    
    @Schema(description = "Indicates if this field can be used in WHERE clauses", example = "true")
    private boolean filterable;
    
    @Schema(description = "Indicates if this field can be requested in SELECT clauses", example = "true")
    private boolean selectable;
}
