package com.vmetrix.querymanager.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Composite filter node: combines child nodes with AND / OR. "
        + "Nesting is arbitrary depth. Corresponds to FilterGroup in the domain model.")
public class FilterGroupDto implements FilterNodeDto {

    @NotBlank(message = "Operator must not be blank in a filter group")
    @Schema(description = "Boolean operator used to combine child conditions.",
            allowableValues = {"AND", "OR"}, example = "AND", required = true)
    private String operator;

    @NotEmpty(message = "Conditions must not be empty in a filter group")
    @Valid
    @Schema(description = "Child filter nodes (groups or conditions). Must not be empty.",
            required = true)
    private List<FilterNodeDto> conditions;
}
