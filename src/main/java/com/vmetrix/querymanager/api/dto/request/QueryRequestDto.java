package com.vmetrix.querymanager.api.dto.request;

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
public class QueryRequestDto {

    @NotEmpty(message = "Select fields must not be empty")
    @Valid
    private List<SelectFieldDto> select;

    @Valid
    private FilterNodeDto filters;

    @Valid
    private List<SortFieldDto> sorting;

    @Positive(message = "maxResults must be positive")
    private Integer maxResults;
}
