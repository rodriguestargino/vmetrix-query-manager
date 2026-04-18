package com.vmetrix.querymanager.domain.engine.builder;

import com.vmetrix.querymanager.application.service.MetadataService;
import com.vmetrix.querymanager.domain.model.EntityMetadata;
import com.vmetrix.querymanager.domain.model.FieldMetadata;
import com.vmetrix.querymanager.domain.model.SortField;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class OrderByClauseBuilder {

    private final MetadataService metadataService;

    public String build(List<SortField> sortFields) {
        if (sortFields == null || sortFields.isEmpty()) {
            return "";
        }

        String orderBy = sortFields.stream()
                .map(this::buildSort)
                .collect(Collectors.joining(", "));

        return "ORDER BY " + orderBy;
    }

    private String buildSort(SortField sortField) {
        EntityMetadata entityMetadata = metadataService.findEntityByLogicalName(sortField.getEntity());
        FieldMetadata fieldMetadata = metadataService.findField(sortField.getEntity(), sortField.getField());

        return entityMetadata.getDefaultAlias() + "." + fieldMetadata.getPhysicalName() + " " + sortField.getDirection().name();
    }
}
