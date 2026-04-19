package com.vmetrix.querymanager.application.service;

import com.vmetrix.querymanager.domain.engine.builder.QuerySpecification;
import com.vmetrix.querymanager.domain.model.ValidationError;

import java.util.List;

public interface QueryValidator {
    List<ValidationError> validate(QuerySpecification spec);
}
