package com.vmetrix.querymanager.application.service;

import com.vmetrix.querymanager.domain.engine.builder.QueryAssembler;
import com.vmetrix.querymanager.domain.engine.builder.QuerySpecification;
import com.vmetrix.querymanager.domain.model.QueryResult;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class QueryServiceImpl implements QueryService {

    private final QueryAssembler queryAssembler;
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public QueryResult buildQuery(QuerySpecification spec) {
        return queryAssembler.assemble(spec);
    }

    @Override
    public List<Map<String, Object>> executeQuery(QuerySpecification spec) {
        QueryResult buildResult = buildQuery(spec);
        return jdbcTemplate.queryForList(buildResult.getSql(), buildResult.getParameters());
    }
}
