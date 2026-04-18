package com.vmetrix.querymanager.domain.engine.builder;

import com.vmetrix.querymanager.application.service.MetadataService;
import com.vmetrix.querymanager.domain.model.EntityMetadata;
import com.vmetrix.querymanager.domain.model.FieldMetadata;
import com.vmetrix.querymanager.domain.model.SelectField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SelectClauseBuilderTest {

    @Mock
    private MetadataService metadataService;

    private SelectClauseBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new SelectClauseBuilder(metadataService);
    }

    @Test
    void should_build_select_clause_with_base_column() {
        // Arrange
        when(metadataService.findEntityByLogicalName("transaction"))
                .thenReturn(EntityMetadata.builder().physicalName("TRANSACTION").defaultAlias("t").build());
        when(metadataService.findField("transaction", "txnDate"))
                .thenReturn(FieldMetadata.builder().physicalName("TXN_DATE").build());

        List<SelectField> fields = List.of(
                SelectField.builder().entity("transaction").field("txnDate").build()
        );

        // Act
        String sql = builder.build(fields);

        // Assert
        assertThat(sql).isEqualTo("SELECT t.TXN_DATE");
    }

    @Test
    void should_build_select_clause_with_aliased_field() {
        // Arrange
        when(metadataService.findEntityByLogicalName("transaction"))
                .thenReturn(EntityMetadata.builder().physicalName("TRANSACTION").defaultAlias("t").build());
        when(metadataService.findEntityByLogicalName("counterparty"))
                .thenReturn(EntityMetadata.builder().physicalName("PARTY").defaultAlias("cp").build());
        
        when(metadataService.findField("transaction", "txnDate"))
                .thenReturn(FieldMetadata.builder().physicalName("TXN_DATE").build());
        when(metadataService.findField("counterparty", "partyName"))
                .thenReturn(FieldMetadata.builder().physicalName("PARTY_NAME").build());

        List<SelectField> fields = List.of(
                SelectField.builder().entity("transaction").field("txnDate").build(),
                SelectField.builder().entity("counterparty").field("partyName").alias("counterpartyName").build()
        );

        // Act
        String sql = builder.build(fields);

        // Assert
        assertThat(sql).isEqualTo("SELECT t.TXN_DATE, cp.PARTY_NAME AS counterpartyName");
    }
}
