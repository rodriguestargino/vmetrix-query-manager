package com.vmetrix.querymanager.api.dto.request;

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
public class SortFieldDto {

    @NotBlank(message = "Entity name must not be blank")
    private String entity;

    @NotBlank(message = "Field name must not be blank")
    private String field;

    @NotNull(message = "Direction must not be null")
    private SortDirectionDto direction;
}
