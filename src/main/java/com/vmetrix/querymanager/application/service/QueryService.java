package com.vmetrix.querymanager.application.service;

import com.vmetrix.querymanager.domain.engine.builder.QuerySpecification;
import com.vmetrix.querymanager.domain.model.QueryResult;

public interface QueryService {
    QueryResult buildQuery(QuerySpecification spec);
}
