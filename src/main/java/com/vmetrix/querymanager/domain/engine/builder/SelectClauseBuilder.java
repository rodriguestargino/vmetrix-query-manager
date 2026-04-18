package com.vmetrix.querymanager.domain.engine.builder;

import com.vmetrix.querymanager.application.service.MetadataService;
import com.vmetrix.querymanager.domain.model.EntityMetadata;
import com.vmetrix.querymanager.domain.model.FieldMetadata;
import com.vmetrix.querymanager.domain.model.SelectField;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class SelectClauseBuilder {

    private final MetadataService metadataService;

    public String build(List<SelectField> fields) {
        if (fields == null || fields.isEmpty()) {
            throw new IllegalArgumentException("SELECT fields cannot be empty");
        }

        String columns = fields.stream()
                .map(this::buildColumn)
                .collect(Collectors.joining(", "));

        return "SELECT " + columns;
    }

    private String buildColumn(SelectField field) {
        EntityMetadata entityMetadata = metadataService.findEntityByLogicalName(field.getEntity());
        FieldMetadata fieldMetadata = metadataService.findField(field.getEntity(), field.getField());

        String baseColumn = entityMetadata.getDefaultAlias() + "." + fieldMetadata.getPhysicalName();
        if (field.getAlias() != null && !field.getAlias().isBlank()) {
            return baseColumn + " AS " + field.getAlias();
        }
        return baseColumn;
    }
}
