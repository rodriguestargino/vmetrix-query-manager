package com.vmetrix.querymanager.api.mapper;

import com.vmetrix.querymanager.api.dto.request.FilterConditionDto;
import com.vmetrix.querymanager.api.dto.request.FilterGroupDto;
import com.vmetrix.querymanager.api.dto.request.FilterNodeDto;
import com.vmetrix.querymanager.api.dto.request.QueryRequestDto;
import com.vmetrix.querymanager.domain.engine.builder.QuerySpecification;
import com.vmetrix.querymanager.domain.model.FilterCondition;
import com.vmetrix.querymanager.domain.model.FilterGroup;
import com.vmetrix.querymanager.domain.model.FilterNode;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface QueryRequestMapper {

    @Mapping(source = "select", target = "selectFields")
    @Mapping(source = "filters", target = "filterBaseNode")
    @Mapping(source = "sorting", target = "sortFields")
    @Mapping(target = "baseEntity", expression = "java(extractBaseEntity(dto))")
    @Mapping(target = "allRequestedAliases", expression = "java(extractAllAliases(dto))")
    QuerySpecification toDomain(QueryRequestDto dto);

    default String extractBaseEntity(QueryRequestDto dto) {
        if (dto != null && dto.getSelect() != null && !dto.getSelect().isEmpty()) {
            return dto.getSelect().get(0).getEntity();
        }
        return null;
    }

    default java.util.Set<String> extractAllAliases(QueryRequestDto dto) {
        java.util.Set<String> aliases = new java.util.HashSet<>();
        if (dto == null) return aliases;
        
        if (dto.getSelect() != null) {
            dto.getSelect().forEach(s -> aliases.add(s.getEntity()));
        }
        if (dto.getSorting() != null) {
            dto.getSorting().forEach(s -> aliases.add(s.getEntity()));
        }
        if (dto.getFilters() != null) {
            extractAliasesFromFilter(dto.getFilters(), aliases);
        }
        return aliases;
    }

    default void extractAliasesFromFilter(FilterNodeDto node, java.util.Set<String> aliases) {
        if (node instanceof FilterGroupDto groupDto && groupDto.getConditions() != null) {
            groupDto.getConditions().forEach(c -> extractAliasesFromFilter(c, aliases));
        } else if (node instanceof FilterConditionDto condDto && condDto.getEntity() != null) {
            aliases.add(condDto.getEntity());
        }
    }

    default FilterNode mapFilterNode(FilterNodeDto dto) {
        if (dto == null) {
            return null;
        }
        if (dto instanceof FilterGroupDto groupDto) {
            return FilterGroup.builder()
                    .operator(groupDto.getOperator())
                    .conditions(groupDto.getConditions().stream()
                            .map(this::mapFilterNode)
                            .collect(Collectors.toList()))
                    .build();
        } else if (dto instanceof FilterConditionDto conditionDto) {
            return FilterCondition.builder()
                    .entity(conditionDto.getEntity())
                    .field(conditionDto.getField())
                    .comparator(conditionDto.getComparator())
                    .value(conditionDto.getValue())
                    .build();
        }
        return null;
    }
}
