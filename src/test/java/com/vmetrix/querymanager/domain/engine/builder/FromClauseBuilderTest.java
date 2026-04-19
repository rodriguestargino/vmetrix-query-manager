package com.vmetrix.querymanager.domain.engine.builder;

import com.vmetrix.querymanager.domain.model.EntityMetadata;
import com.vmetrix.querymanager.domain.port.MetadataCatalog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FromClauseBuilderTest {

    @Mock
    private MetadataCatalog metadataService;

    private FromClauseBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new FromClauseBuilder(metadataService);
    }

    @Test
    void should_build_from_clause() {
        // Arrange
        when(metadataService.findEntityByLogicalName("transaction"))
                .thenReturn(EntityMetadata.builder().physicalName("TRANSACTION").defaultAlias("t").build());

        // Act
        String sql = builder.build("transaction");

        // Assert
        assertThat(sql).isEqualTo("FROM TRANSACTION t");
    }
}
