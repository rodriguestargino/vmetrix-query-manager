package com.vmetrix.querymanager.domain.port;

import com.vmetrix.querymanager.domain.model.EntityMetadata;
import com.vmetrix.querymanager.domain.model.FieldMetadata;
import com.vmetrix.querymanager.domain.model.RelationshipMetadata;

import java.util.List;

public interface MetadataRepository {
    List<EntityMetadata> findAllEntities();
    List<FieldMetadata> findAllFields();
    List<RelationshipMetadata> findAllRelationships();
}
