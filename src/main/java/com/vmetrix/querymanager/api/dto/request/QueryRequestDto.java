package com.vmetrix.querymanager.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "High-level query specification consumed by POST /api/query/build "
        + "and POST /api/query/validate. Fields are logical (camelCase) and are resolved "
        + "to physical SQL names via metadata lookup.")
public class QueryRequestDto {

    @NotEmpty(message = "Select fields must not be empty")
    @Valid
    @Schema(description = "Columns to project in the SELECT clause. Must not be empty.",
            required = true)
    private List<SelectFieldDto> select;

    @Valid
    @Schema(description = "Root filter tree (AND/OR group or a leaf condition). "
            + "Optional — omit for an unfiltered query.")
    private FilterNodeDto filters;

    @Valid
    @Schema(description = "Optional ORDER BY clause entries, applied in order.")
    private List<SortFieldDto> sorting;

    @Positive(message = "maxResults must be positive")
    @Schema(description = "Optional row limit. When provided, generates "
            + "'FETCH FIRST N ROWS ONLY' (Oracle-compatible). Must be > 0.",
            example = "500")
    private Integer maxResults;
}
