package com.vmetrix.querymanager.api.mapper;

import com.vmetrix.querymanager.api.dto.response.EntityMetadataDto;
import com.vmetrix.querymanager.api.dto.response.FieldMetadataDto;
import com.vmetrix.querymanager.api.dto.response.RelationMetadataDto;
import com.vmetrix.querymanager.domain.model.EntityDefinition;
import com.vmetrix.querymanager.domain.model.FieldMetadata;
import com.vmetrix.querymanager.domain.model.RelationshipMetadata;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EntityMetadataMapper {

    @Mapping(target = "entity", source = "entityMetadata.logicalName")
    @Mapping(target = "physicalTable", source = "entityMetadata.physicalName")
    @Mapping(target = "alias", source = "entityMetadata.defaultAlias")
    @Mapping(target = "relations", source = "relationships")
    EntityMetadataDto toDto(EntityDefinition source);

    List<EntityMetadataDto> toDtoList(List<EntityDefinition> sourceList);

    @Mapping(target = "name", source = "logicalName")
    @Mapping(target = "type", source = "dataType")
    @Mapping(target = "primaryKey", source = "pk")
    FieldMetadataDto toFieldDto(FieldMetadata fieldMetadata);

    @Mapping(target = "alias", source = "relationAlias")
    @Mapping(target = "sourceField", source = "sourceColumn")
    @Mapping(target = "targetField", source = "targetColumn")
    RelationMetadataDto toRelationDto(RelationshipMetadata relationshipMetadata);
}
