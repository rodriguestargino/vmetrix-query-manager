package com.vmetrix.querymanager.api.dto.request;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Marker interface for a node in the Composite filter tree.
 *
 * <p>Jackson deduces the concrete type at deserialization time based on the
 * presence of {@code operator+conditions} (group) or {@code entity+field+comparator}
 * (condition).
 */
@JsonTypeInfo(use = Id.DEDUCTION)
@JsonSubTypes({
    @JsonSubTypes.Type(FilterGroupDto.class),
    @JsonSubTypes.Type(FilterConditionDto.class)
})
@Schema(description = "Polymorphic filter node: either a FilterGroupDto (AND/OR composite) "
        + "or a FilterConditionDto (leaf predicate). Jackson resolves the concrete type from "
        + "the payload shape.",
        oneOf = {FilterGroupDto.class, FilterConditionDto.class})
public interface FilterNodeDto {
}
