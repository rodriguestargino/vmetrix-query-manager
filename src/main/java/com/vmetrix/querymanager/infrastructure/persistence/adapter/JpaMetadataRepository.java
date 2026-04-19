package com.vmetrix.querymanager.infrastructure.persistence.adapter;

import com.vmetrix.querymanager.domain.model.EntityMetadata;
import com.vmetrix.querymanager.domain.model.FieldMetadata;
import com.vmetrix.querymanager.domain.model.RelationshipMetadata;
import com.vmetrix.querymanager.domain.port.MetadataRepository;
import com.vmetrix.querymanager.infrastructure.persistence.repository.MetaColumnRepository;
import com.vmetrix.querymanager.infrastructure.persistence.repository.MetaRelationshipRepository;
import com.vmetrix.querymanager.infrastructure.persistence.repository.MetaEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JpaMetadataRepository implements MetadataRepository {

    private final MetaEntityRepository entityRepo;
    private final MetaColumnRepository columnRepo;
    private final MetaRelationshipRepository relationshipRepo;

    @Override
    public List<EntityMetadata> findAllEntities() {
        return entityRepo.findAll().stream()
                .map(jpa -> EntityMetadata.builder()
                        .logicalName(jpa.getEntityName())
                        .physicalName(jpa.getPhysicalTable())
                        .defaultAlias(jpa.getDefaultAlias())
                        .description(jpa.getDescription())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<FieldMetadata> findAllFields() {
        return columnRepo.findAll().stream()
                .map(jpa -> FieldMetadata.builder()
                        .entityLogicalName(jpa.getMetaEntity().getEntityName())
                        .logicalName(jpa.getLogicalName())
                        .physicalName(jpa.getPhysicalName())
                        .dataType(jpa.getDataType())
                        .isPk(jpa.getIsPk() != null && jpa.getIsPk() == 1)
                        .isFk(jpa.getIsFk() != null && jpa.getIsFk() == 1)
                        .fkEntity(jpa.getFkTargetEntity())
                        .fkColumn(jpa.getFkTargetColumn())
                        .isFilterable(jpa.getIsFilterable() != null && jpa.getIsFilterable() == 1)
                        .isSelectable(jpa.getIsSelectable() != null && jpa.getIsSelectable() == 1)
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<RelationshipMetadata> findAllRelationships() {
        return relationshipRepo.findAll().stream()
                .map(jpa -> RelationshipMetadata.builder()
                        .sourceEntity(jpa.getSourceEntity().getEntityName())
                        .sourceColumn(jpa.getSourceColumn())
                        .targetEntity(jpa.getTargetEntity().getEntityName())
                        .targetColumn(jpa.getTargetColumn())
                        .joinType(jpa.getJoinType())
                        .relationAlias(jpa.getRelationAlias())
                        .build())
                .collect(Collectors.toList());
    }
}
