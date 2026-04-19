package com.vmetrix.querymanager.application.service;

import com.vmetrix.querymanager.domain.engine.builder.QueryAssembler;
import com.vmetrix.querymanager.domain.engine.builder.QuerySpecification;
import com.vmetrix.querymanager.domain.model.QueryResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QueryServiceImpl implements QueryService {

    private final QueryAssembler queryAssembler;

    @Override
    public QueryResult buildQuery(QuerySpecification spec) {
        return queryAssembler.assemble(spec);
    }
}
