package com.vmetrix.querymanager.api.controller;

import com.vmetrix.querymanager.api.dto.request.QueryRequestDto;
import com.vmetrix.querymanager.api.dto.response.QueryBuildResponse;
import com.vmetrix.querymanager.api.dto.response.ValidationResponse;
import com.vmetrix.querymanager.application.service.QueryService;
import com.vmetrix.querymanager.application.service.QueryValidator;
import com.vmetrix.querymanager.domain.model.ValidationError;
import lombok.RequiredArgsConstructor;
import java.util.List;
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
    private final QueryValidator queryValidator;

    @PostMapping("/build")
    public ResponseEntity<QueryBuildResponse> buildQuery(@Valid @RequestBody QueryRequestDto request) {
        return ResponseEntity.ok(queryService.buildQuery(request));
    }

    @PostMapping("/validate")
    public ResponseEntity<ValidationResponse> validateQuery(@Valid @RequestBody QueryRequestDto request) {
        List<ValidationError> errors = queryValidator.validate(request);
        if (errors.isEmpty()) {
            return ResponseEntity.ok(ValidationResponse.builder().valid(true).build());
        }
        return ResponseEntity.badRequest().body(ValidationResponse.builder().valid(false).errors(errors).build());
    }
}
