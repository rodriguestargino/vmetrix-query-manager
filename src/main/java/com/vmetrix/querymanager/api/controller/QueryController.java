package com.vmetrix.querymanager.api.controller;

import com.vmetrix.querymanager.api.documentation.QueryControllerDoc;
import com.vmetrix.querymanager.api.dto.request.QueryRequestDto;
import com.vmetrix.querymanager.api.dto.response.QueryBuildResponse;
import com.vmetrix.querymanager.api.dto.response.ValidationErrorDto;
import com.vmetrix.querymanager.api.dto.response.ValidationResponse;
import com.vmetrix.querymanager.api.mapper.QueryBuildResponseMapper;
import com.vmetrix.querymanager.api.mapper.QueryRequestMapper;
import com.vmetrix.querymanager.api.mapper.ValidationErrorMapper;
import com.vmetrix.querymanager.application.service.QueryService;
import com.vmetrix.querymanager.application.service.QueryValidator;
import com.vmetrix.querymanager.domain.engine.builder.QuerySpecification;
import com.vmetrix.querymanager.domain.model.QueryResult;
import com.vmetrix.querymanager.domain.model.ValidationError;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/query")
@RequiredArgsConstructor
@Validated
@Tag(name = "Query API",
        description = "Endpoints that turn a high-level query specification into "
                + "parameterized SQL or validate it without generating SQL.")
public class QueryController implements QueryControllerDoc {

    private final QueryService queryService;
    private final QueryValidator queryValidator;
    private final QueryRequestMapper queryRequestMapper;
    private final QueryBuildResponseMapper queryBuildResponseMapper;
    private final ValidationErrorMapper validationErrorMapper;

    @Override
    @PostMapping("/build")
    public ResponseEntity<QueryBuildResponse> buildQuery(QueryRequestDto request) {
        QuerySpecification spec = queryRequestMapper.toDomain(request);
        QueryResult result = queryService.buildQuery(spec);
        return ResponseEntity.ok(queryBuildResponseMapper.toDto(result));
    }

    @Override
    @PostMapping("/execute")
    public ResponseEntity<List<Map<String, Object>>> executeQuery(QueryRequestDto request) {
        QuerySpecification spec = queryRequestMapper.toDomain(request);
        List<Map<String, Object>> result = queryService.executeQuery(spec);
        return ResponseEntity.ok(result);
    }

    @Override
    @PostMapping("/validate")
    public ResponseEntity<ValidationResponse> validateQuery(QueryRequestDto request) {
        QuerySpecification spec = queryRequestMapper.toDomain(request);
        List<ValidationError> errors = queryValidator.validate(spec);
        if (errors.isEmpty()) {
            return ResponseEntity.ok(ValidationResponse.builder().valid(true).build());
        }
        List<ValidationErrorDto> errorDtos = validationErrorMapper.toDtoList(errors);
        return ResponseEntity.badRequest()
                .body(ValidationResponse.builder().valid(false).errors(errorDtos).build());
    }
}
