package com.vmetrix.querymanager.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Extra metadata describing a generated query")
public class QueryMetadataDto {

    @Schema(description = "Number of columns projected in the SELECT clause.", example = "2")
    private int columnCount;

    @Schema(description = "Total number of leaf filter conditions in the WHERE clause.",
            example = "1")
    private int filterCount;

    @Schema(description = "ISO-8601 UTC timestamp captured at generation time.",
            example = "2026-04-18T00:00:00Z")
    private String generatedAt;
}
