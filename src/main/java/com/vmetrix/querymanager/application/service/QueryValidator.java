package com.vmetrix.querymanager.application.service;

import com.vmetrix.querymanager.api.dto.request.QueryRequestDto;
import com.vmetrix.querymanager.domain.model.ValidationError;

import java.util.List;

public interface QueryValidator {
    List<ValidationError> validate(QueryRequestDto request);
}
