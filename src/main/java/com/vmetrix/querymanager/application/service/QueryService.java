package com.vmetrix.querymanager.application.service;

import com.vmetrix.querymanager.domain.engine.builder.QuerySpecification;
import com.vmetrix.querymanager.domain.model.QueryResult;

import java.util.List;
import java.util.Map;

public interface QueryService {
    QueryResult buildQuery(QuerySpecification spec);
    List<Map<String, Object>> executeQuery(QuerySpecification spec);
}
