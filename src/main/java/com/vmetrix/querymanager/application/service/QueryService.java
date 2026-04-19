package com.vmetrix.querymanager.application.service;

import com.vmetrix.querymanager.api.dto.request.QueryRequestDto;
import com.vmetrix.querymanager.api.dto.response.QueryBuildResponse;

public interface QueryService {
    QueryBuildResponse buildQuery(QueryRequestDto requestDto);
}
