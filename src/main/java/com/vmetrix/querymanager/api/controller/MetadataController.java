package com.vmetrix.querymanager.api.controller;

import com.vmetrix.querymanager.api.documentation.MetadataControllerDoc;
import com.vmetrix.querymanager.api.dto.response.EntityMetadataDto;
import com.vmetrix.querymanager.api.mapper.EntityMetadataMapper;
import com.vmetrix.querymanager.application.service.MetadataService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/metadata")
@RequiredArgsConstructor
@Tag(name = "Metadata API",
        description = "Endpoints for discovering the query engine data model — entities, fields, "
                + "relationships, and valid comparators per data type.")
public class MetadataController implements MetadataControllerDoc {

    private final MetadataService metadataService;
    private final EntityMetadataMapper mapper;

    @Override
    @GetMapping("/entities")
    public ResponseEntity<List<EntityMetadataDto>> getAllEntities() {
        return ResponseEntity.ok(mapper.toDtoList(metadataService.getAllEntities()));
    }

    @Override
    @GetMapping("/comparators")
    public ResponseEntity<Map<String, List<String>>> getComparators() {
        return ResponseEntity.ok(metadataService.getComparators());
    }
}
