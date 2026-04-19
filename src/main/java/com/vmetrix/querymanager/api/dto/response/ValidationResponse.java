package com.vmetrix.querymanager.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response returned by POST /api/query/validate — structured validation outcome")
public class ValidationResponse {

    @Schema(description = "True when the submitted query specification is valid; false otherwise",
            example = "false")
    private boolean valid;

    @Schema(description = "All validation errors found (empty when valid=true). "
            + "The validator never fails fast — the full list is always returned.")
    @Builder.Default
    private List<ValidationErrorDto> errors = new java.util.ArrayList<>();
}
