package com.vmetrix.querymanager.api.mapper;

import com.vmetrix.querymanager.api.dto.response.QueryBuildResponse;
import com.vmetrix.querymanager.domain.model.QueryResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Mapper(componentModel = "spring")
public interface QueryBuildResponseMapper {

    @Mapping(target = "metadata.columnCount", source = "columnCount")
    @Mapping(target = "metadata.filterCount", source = "filterCount")
    @Mapping(target = "metadata.generatedAt", source = "generatedAt")
    QueryBuildResponse toDto(QueryResult domain);

    default String map(Instant value) {
        return value == null ? null : DateTimeFormatter.ISO_INSTANT.withZone(ZoneOffset.UTC).format(value);
    }
}
