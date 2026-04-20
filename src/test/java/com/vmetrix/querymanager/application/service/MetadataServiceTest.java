package com.vmetrix.querymanager.application.service;

import com.vmetrix.querymanager.domain.model.EntityMetadata;
import com.vmetrix.querymanager.domain.model.FieldMetadata;
import com.vmetrix.querymanager.domain.model.RelationshipMetadata;
import com.vmetrix.querymanager.domain.port.MetadataRepository;
import com.vmetrix.querymanager.shared.exception.UnknownEntityException;
import com.vmetrix.querymanager.shared.exception.UnknownFieldException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MetadataServiceTest {

    @Mock
    private MetadataRepository metadataRepository;

    @InjectMocks
    private MetadataService metadataService;

    @BeforeEach
    void setUp() {
        EntityMetadata txEntity = EntityMetadata.builder()
                .logicalName("transaction")
                .physicalName("TRANSACTION")
                .defaultAlias("t")
                .build();

        FieldMetadata txnDate = FieldMetadata.builder()
                .entityLogicalName("transaction")
                .logicalName("txnDate")
                .physicalName("TXN_DATE")
                .dataType("date")
                .isFilterable(true)
                .isSelectable(true)
                .build();

        RelationshipMetadata txToParty = RelationshipMetadata.builder()
                .sourceEntity("transaction")
                .sourceColumn("counterpartyId")
                .targetEntity("party")
                .targetColumn("partyId")
                .relationAlias("counterparty")
                .joinType("LEFT JOIN")
                .build();

        when(metadataRepository.findAllEntities()).thenReturn(Collections.singletonList(txEntity));
        when(metadataRepository.findAllFields()).thenReturn(Collections.singletonList(txnDate));
        when(metadataRepository.findAllRelationships()).thenReturn(Collections.singletonList(txToParty));

        metadataService.initCache();
    }

    @Test
    void should_return_entity_metadata_when_found_by_logical_name() {
        EntityMetadata result = metadataService.findEntityByLogicalName("transaction");
        
        assertThat(result).isNotNull();
        assertThat(result.getLogicalName()).isEqualTo("transaction");
        assertThat(result.getPhysicalName()).isEqualTo("TRANSACTION");
    }

    @Test
    void should_return_field_metadata_when_found_by_entity_and_logical_name() {
        FieldMetadata result = metadataService.findField("transaction", "txnDate");
        
        assertThat(result).isNotNull();
        assertThat(result.getLogicalName()).isEqualTo("txnDate");
        assertThat(result.getPhysicalName()).isEqualTo("TXN_DATE");
    }

    @Test
    void should_return_relationships_when_found_by_source_entity() {
        List<RelationshipMetadata> result = metadataService.findRelationships("transaction");
        
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRelationAlias()).isEqualTo("counterparty");
    }

    @Test
    void should_throw_unknown_entity_exception_when_entity_not_found() {
        assertThatThrownBy(() -> metadataService.findEntityByLogicalName("unknown"))
                .isInstanceOf(UnknownEntityException.class)
                .hasMessageContaining("Unknown entity: 'unknown'");
    }

    @Test
    void should_throw_unknown_field_exception_when_field_not_found() {
        assertThatThrownBy(() -> metadataService.findField("transaction", "unknownField"))
                .isInstanceOf(UnknownFieldException.class)
                .hasMessageContaining("Unknown field: 'unknownField' is not defined for entity 'transaction'");
    }
}
