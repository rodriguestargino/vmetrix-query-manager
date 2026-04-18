package com.vmetrix.querymanager.domain.engine.builder;

import com.vmetrix.querymanager.application.service.MetadataService;
import com.vmetrix.querymanager.domain.model.EntityMetadata;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FromClauseBuilder {

    private final MetadataService metadataService;

    public String build(String logicalEntityName) {
        EntityMetadata entityMetadata = metadataService.findEntityByLogicalName(logicalEntityName);
        return "FROM " + entityMetadata.getPhysicalName() + " " + entityMetadata.getDefaultAlias();
    }
}
