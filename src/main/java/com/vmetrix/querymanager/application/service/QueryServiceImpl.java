package com.vmetrix.querymanager.application.service;

import com.vmetrix.querymanager.api.dto.request.QueryRequestDto;
import com.vmetrix.querymanager.api.dto.response.QueryBuildResponse;
import com.vmetrix.querymanager.api.dto.response.QueryMetadataDto;
import com.vmetrix.querymanager.api.mapper.QueryRequestMapper;
import com.vmetrix.querymanager.domain.engine.builder.QueryAssembler;
import com.vmetrix.querymanager.domain.engine.builder.QuerySpecification;
import com.vmetrix.querymanager.domain.model.QueryResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class QueryServiceImpl implements QueryService {

    private final QueryRequestMapper queryRequestMapper;
    private final QueryAssembler queryAssembler;

    @Override
    public QueryBuildResponse buildQuery(QueryRequestDto requestDto) {
        // Map DTO to Domain
        QuerySpecification spec = queryRequestMapper.toDomain(requestDto);

        // Core business logic delegation
        QueryResult result = queryAssembler.assemble(spec);

        // Assemble Response DTO
        return QueryBuildResponse.builder()
                .sql(result.getSql())
                .parameters(result.getParameters())
                .resolvedTables(result.getResolvedTables())
                .resolvedJoins(result.getResolvedJoins())
                .metadata(QueryMetadataDto.builder()
                        .columnCount(result.getColumnCount())
                        .filterCount(result.getFilterCount())
                        .generatedAt(ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT))
                        .build())
                .build();
    }
}
