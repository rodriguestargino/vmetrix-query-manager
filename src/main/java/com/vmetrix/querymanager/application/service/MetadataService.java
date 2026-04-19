package com.vmetrix.querymanager.application.service;

import com.vmetrix.querymanager.domain.model.EntityDefinition;
import com.vmetrix.querymanager.domain.model.EntityMetadata;
import com.vmetrix.querymanager.domain.model.FieldMetadata;
import com.vmetrix.querymanager.domain.model.RelationshipMetadata;
import com.vmetrix.querymanager.domain.port.MetadataRepository;
import com.vmetrix.querymanager.shared.exception.UnknownEntityException;
import com.vmetrix.querymanager.shared.exception.UnknownFieldException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetadataService implements com.vmetrix.querymanager.domain.port.MetadataCatalog {

    private final MetadataRepository metadataRepository;

    // Cache structures
    // logical entity name -> EntityMetadata
    private final Map<String, EntityMetadata> entityCache = new ConcurrentHashMap<>();
    
    // logical entity name -> (logical field name -> FieldMetadata)
    private final Map<String, Map<String, FieldMetadata>> fieldCache = new ConcurrentHashMap<>();

    // source logical entity name -> List<RelationshipMetadata>
    private final Map<String, List<RelationshipMetadata>> relationshipCache = new ConcurrentHashMap<>();

    // alias -> target logical entity name
    private final Map<String, String> aliasCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void initCache() {
        log.info("Loading metadata cache...");
        
        entityCache.clear();
        fieldCache.clear();
        relationshipCache.clear();
        aliasCache.clear();

        // 1. Load entities
        List<EntityMetadata> entities = metadataRepository.findAllEntities();
        for (EntityMetadata entity : entities) {
            entityCache.put(entity.getLogicalName(), entity);
            fieldCache.put(entity.getLogicalName(), new ConcurrentHashMap<>());
        }

        // 2. Load fields
        List<FieldMetadata> fields = metadataRepository.findAllFields();
        for (FieldMetadata field : fields) {
            Map<String, FieldMetadata> entityFields = fieldCache.get(field.getEntityLogicalName());
            if (entityFields != null) {
                entityFields.put(field.getLogicalName(), field);
            } else {
                log.warn("Field {} belongs to unknown entity {}", field.getLogicalName(), field.getEntityLogicalName());
            }
        }

        // 3. Load relationships
        List<RelationshipMetadata> relationships = metadataRepository.findAllRelationships();
        Map<String, List<RelationshipMetadata>> groupedRels = relationships.stream()
                .collect(Collectors.groupingBy(RelationshipMetadata::getSourceEntity));
        relationshipCache.putAll(groupedRels);

        // 4. Load aliases
        for (RelationshipMetadata rel : relationships) {
            aliasCache.put(rel.getRelationAlias(), rel.getTargetEntity());
        }

        log.info("Metadata cache loaded. Entities: {}, Fields: {}, Relationships: {}, Aliases: {}", 
                entities.size(), fields.size(), relationships.size(), aliasCache.size());
        if (log.isDebugEnabled()) {
             aliasCache.forEach((k, v) -> log.debug("Alias mapping: {} -> {}", k, v));
        }
    }

    @Override
    public EntityMetadata findEntityByLogicalName(String logicalName) {
        EntityMetadata metadata = entityCache.get(logicalName);
        if (metadata == null) {
            throw new UnknownEntityException(logicalName);
        }
        return metadata;
    }

    @Override
    public EntityMetadata findEntityByAlias(String alias) {
        String targetEntity = aliasCache.get(alias);
        if (targetEntity != null) {
            return findEntityByLogicalName(targetEntity);
        }
        return findEntityByLogicalName(alias);
    }

    @Override
    public FieldMetadata findField(String logicalEntityName, String logicalFieldName) {
        Map<String, FieldMetadata> fields = fieldCache.get(logicalEntityName);
        if (fields == null || !fields.containsKey(logicalFieldName)) {
            throw new UnknownFieldException(logicalEntityName, logicalFieldName);
        }
        return fields.get(logicalFieldName);
    }

    @Override
    public List<RelationshipMetadata> findRelationships(String sourceLogicalEntityName) {
        return relationshipCache.getOrDefault(sourceLogicalEntityName, Collections.emptyList());
    }

    @Override
    public List<EntityDefinition> getAllEntities() {
        return entityCache.values().stream()
                .map(entity -> EntityDefinition.builder()
                        .entityMetadata(entity)
                        .fields(new ArrayList<>(fieldCache.getOrDefault(entity.getLogicalName(), Collections.emptyMap()).values()))
                        .relationships(new ArrayList<>(relationshipCache.getOrDefault(entity.getLogicalName(), Collections.emptyList())))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, List<String>> getComparators() {
        return Map.of(
            "string", Arrays.asList("equals", "notEquals", "like", "in", "notIn", "isNull", "isNotNull"),
            "number", Arrays.asList("equals", "notEquals", "greaterThan", "lessThan", "greaterOrEqual", "lessOrEqual", "between", "in", "isNull", "isNotNull"),
            "date", Arrays.asList("equals", "notEquals", "greaterThan", "lessThan", "greaterOrEqual", "lessOrEqual", "between", "isNull", "isNotNull"),
            "timestamp", Arrays.asList("equals", "notEquals", "greaterThan", "lessThan", "greaterOrEqual", "lessOrEqual", "isNull", "isNotNull")
        );
    }
}
