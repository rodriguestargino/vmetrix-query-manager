package com.vmetrix.querymanager.api.controller;

import com.vmetrix.querymanager.api.dto.response.EntityMetadataDto;
import com.vmetrix.querymanager.api.mapper.EntityMetadataMapper;
import com.vmetrix.querymanager.application.service.MetadataService;
import io.swagger.v3.oas.annotations.Operation;
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
@Tag(name = "Metadata API", description = "Endpoints for discovering the query engine data model")
public class MetadataController {

    private final MetadataService metadataService;
    private final EntityMetadataMapper mapper;

    @GetMapping("/entities")
    @Operation(summary = "Get all queryable entities", description = "Returns a tree of all entities, their fields, and relationships")
    public ResponseEntity<List<EntityMetadataDto>> getAllEntities() {
        return ResponseEntity.ok(mapper.toDtoList(metadataService.getAllEntities()));
    }

    @GetMapping("/comparators")
    @Operation(summary = "Get valid comparators", description = "Returns valid SQL operators grouped by data type")
    public ResponseEntity<Map<String, List<String>>> getComparators() {
        return ResponseEntity.ok(metadataService.getComparators());
    }
}
