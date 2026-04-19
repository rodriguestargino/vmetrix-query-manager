package com.vmetrix.querymanager.api.dto.request;

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
public class FilterGroupDto implements FilterNodeDto {

    @NotBlank(message = "Operator must not be blank in a filter group")
    private String operator;

    @NotEmpty(message = "Conditions must not be empty in a filter group")
    @Valid
    private List<FilterNodeDto> conditions;
}
