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
public class SelectFieldDto {

    @NotBlank(message = "Entity name must not be blank")
    private String entity;

    @NotBlank(message = "Field name must not be blank")
    private String field;

    private String alias;
}
