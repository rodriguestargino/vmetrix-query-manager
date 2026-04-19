package com.vmetrix.querymanager.api.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilterConditionDto implements FilterNodeDto {

    @NotBlank(message = "Entity name must not be blank in a filter condition")
    private String entity;

    @NotBlank(message = "Field name must not be blank in a filter condition")
    private String field;

    @NotBlank(message = "Comparator must not be blank in a filter condition")
    private String comparator;

    private Object value;
}
