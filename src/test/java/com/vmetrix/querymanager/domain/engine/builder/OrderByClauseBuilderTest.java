package com.vmetrix.querymanager.domain.engine.builder;

import com.vmetrix.querymanager.application.service.MetadataService;
import com.vmetrix.querymanager.domain.model.EntityMetadata;
import com.vmetrix.querymanager.domain.model.FieldMetadata;
import com.vmetrix.querymanager.domain.model.SortDirection;
import com.vmetrix.querymanager.domain.model.SortField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderByClauseBuilderTest {

    @Mock
    private MetadataService metadataService;

    private OrderByClauseBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new OrderByClauseBuilder(metadataService);
    }

    @Test
    void should_build_empty_order_by_when_no_sort_fields() {
        String sql = builder.build(null);
        assertThat(sql).isEmpty();
        
        sql = builder.build(List.of());
        assertThat(sql).isEmpty();
    }

    @Test
    void should_build_order_by_clause_desc() {
        // Arrange
        when(metadataService.findEntityByLogicalName("transaction"))
                .thenReturn(EntityMetadata.builder().physicalName("TRANSACTION").defaultAlias("t").build());
        when(metadataService.findField("transaction", "txnDate"))
                .thenReturn(FieldMetadata.builder().physicalName("TXN_DATE").build());

        List<SortField> sorting = List.of(
                SortField.builder().entity("transaction").field("txnDate").direction(SortDirection.DESC).build()
        );

        // Act
        String sql = builder.build(sorting);

        // Assert
        assertThat(sql).isEqualTo("ORDER BY t.TXN_DATE DESC");
    }
    
    @Test
    void should_build_order_by_clause_asc() {
        // Arrange
        when(metadataService.findEntityByLogicalName("transaction"))
                .thenReturn(EntityMetadata.builder().physicalName("TRANSACTION").defaultAlias("t").build());
        when(metadataService.findField("transaction", "status"))
                .thenReturn(FieldMetadata.builder().physicalName("STATUS").build());

        List<SortField> sorting = List.of(
                SortField.builder().entity("transaction").field("status").direction(SortDirection.ASC).build()
        );

        // Act
        String sql = builder.build(sorting);

        // Assert
        assertThat(sql).isEqualTo("ORDER BY t.STATUS ASC");
    }
}
