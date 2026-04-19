package com.vmetrix.querymanager.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A single ORDER BY clause entry")
public class SortFieldDto {

    @NotBlank(message = "Entity name must not be blank")
    @Schema(description = "Logical entity name or relation alias.",
            example = "transaction", required = true)
    private String entity;

    @NotBlank(message = "Field name must not be blank")
    @Schema(description = "Logical field name (camelCase) to sort by.",
            example = "txnDate", required = true)
    private String field;

    @NotNull(message = "Direction must not be null")
    @Schema(description = "Sort direction.", example = "DESC", required = true)
    private SortDirectionDto direction;
}
