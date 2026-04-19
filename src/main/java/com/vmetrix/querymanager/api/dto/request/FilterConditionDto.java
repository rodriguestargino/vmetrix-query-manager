package com.vmetrix.querymanager.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Leaf filter node: a single predicate applied to an entity/field "
        + "with a comparator and optional value. Corresponds to FilterCondition in the "
        + "domain model.")
public class FilterConditionDto implements FilterNodeDto {

    @NotBlank(message = "Entity name must not be blank in a filter condition")
    @Schema(description = "Logical entity name or relation alias.",
            example = "transaction", required = true)
    private String entity;

    @NotBlank(message = "Field name must not be blank in a filter condition")
    @Schema(description = "Logical field name (camelCase).",
            example = "status", required = true)
    private String field;

    @NotBlank(message = "Comparator must not be blank in a filter condition")
    @Schema(description = "Comparator name. Validity depends on the field's data type. "
            + "See GET /api/metadata/comparators.",
            example = "equals", required = true)
    private String comparator;

    @Schema(description = "Value for the predicate. Shape depends on the comparator: "
            + "scalar for equals/like/>/<; list (size 2) for 'between'; list for 'in'/'notIn'; "
            + "omitted for 'isNull'/'isNotNull'. Always bound as named parameter — never concatenated.",
            example = "SETTLED")
    private Object value;
}
