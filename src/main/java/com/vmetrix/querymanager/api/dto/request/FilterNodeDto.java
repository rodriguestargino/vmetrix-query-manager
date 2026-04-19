package com.vmetrix.querymanager.api.dto.request;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

@JsonTypeInfo(use = Id.DEDUCTION)
@JsonSubTypes({
    @JsonSubTypes.Type(FilterGroupDto.class),
    @JsonSubTypes.Type(FilterConditionDto.class)
})
public interface FilterNodeDto {
}
