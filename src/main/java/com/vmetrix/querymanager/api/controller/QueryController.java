package com.vmetrix.querymanager.api.controller;

import com.vmetrix.querymanager.api.dto.request.QueryRequestDto;
import com.vmetrix.querymanager.api.dto.response.QueryBuildResponse;
import com.vmetrix.querymanager.application.service.QueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/query")
@RequiredArgsConstructor
@Validated
public class QueryController {

    private final QueryService queryService;

    @PostMapping("/build")
    public ResponseEntity<QueryBuildResponse> buildQuery(@Valid @RequestBody QueryRequestDto request) {
        return ResponseEntity.ok(queryService.buildQuery(request));
    }
}
