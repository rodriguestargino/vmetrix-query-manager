package com.vmetrix.querymanager.domain.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class EntityDefinition {
    private EntityMetadata entityMetadata;
    private List<FieldMetadata> fields;
    private List<RelationshipMetadata> relationships;
}
