package com.vmetrix.querymanager.domain.port;

import com.vmetrix.querymanager.domain.model.EntityDefinition;
import com.vmetrix.querymanager.domain.model.EntityMetadata;
import com.vmetrix.querymanager.domain.model.FieldMetadata;
import com.vmetrix.querymanager.domain.model.RelationshipMetadata;

import java.util.List;
import java.util.Map;

/**
 * Port for metadata lookups required by the query engine.
 */
public interface MetadataCatalog {
    EntityMetadata findEntityByLogicalName(String logicalName);
    EntityMetadata findEntityByAlias(String alias);
    FieldMetadata findField(String logicalEntityName, String logicalFieldName);
    List<RelationshipMetadata> findRelationships(String sourceLogicalEntityName);
    List<EntityDefinition> getAllEntities();
    Map<String, List<String>> getComparators();
}
