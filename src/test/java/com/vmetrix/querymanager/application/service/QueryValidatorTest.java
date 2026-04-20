package com.vmetrix.querymanager.application.service;

import com.vmetrix.querymanager.domain.engine.builder.QuerySpecification;
import com.vmetrix.querymanager.domain.model.*;
import com.vmetrix.querymanager.domain.port.MetadataCatalog;
import com.vmetrix.querymanager.shared.exception.UnknownEntityException;
import com.vmetrix.querymanager.shared.exception.UnknownFieldException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QueryValidatorTest {

    @Mock
    private MetadataCatalog metadataService;

    @InjectMocks
    private QueryValidatorImpl queryValidator;

    @BeforeEach
    void setUp() {
        Map<String, List<String>> comparators = new HashMap<>();
        comparators.put("string", Arrays.asList("equals", "notEquals"));
        comparators.put("number", Arrays.asList("equals", "greaterThan"));
        when(metadataService.getComparators()).thenReturn(comparators);
    }

    @Test
    void should_collect_multiple_errors_in_one_pass() {
        // Arrange
        SelectField s1 = SelectField.builder()
                .entity("transaction")
                .field("nonSelectableField")
                .build();
        
        SelectField s2 = SelectField.builder()
                .entity("unknown_entity")
                .field("foo")
                .build();
        
        FilterCondition f1 = FilterCondition.builder()
                .entity("transaction")
                .field("status")
                .comparator("greaterThan") // valid for number, but status is string
                .value("SETTLED")
                .build();
        
        FilterGroup group = FilterGroup.builder()
                .operator("AND")
                .conditions(Arrays.asList(f1))
                .build();

        SortField sort1 = SortField.builder()
                .entity("transaction")
                .field("unknown_field")
                .direction(SortDirection.ASC)
                .build();

        QuerySpecification spec = QuerySpecification.builder()
                .selectFields(Arrays.asList(s1, s2))
                .filterBaseNode(group)
                .sortFields(Arrays.asList(sort1))
                .build();

        // Mock behaviors
        when(metadataService.findEntityByAlias("transaction")).thenReturn(
                EntityMetadata.builder().logicalName("transaction").build());
        when(metadataService.findEntityByAlias("unknown_entity")).thenThrow(new UnknownEntityException("unknown_entity"));

        when(metadataService.findField("transaction", "nonSelectableField")).thenReturn(
                FieldMetadata.builder().logicalName("nonSelectableField").dataType("string").isSelectable(false).build());
        
        when(metadataService.findField("transaction", "status")).thenReturn(
                FieldMetadata.builder().logicalName("status").dataType("string").isFilterable(true).build());
                
        when(metadataService.findField("transaction", "unknown_field")).thenThrow(new UnknownFieldException("transaction", "unknown_field"));

        // Act
        List<ValidationError> errors = queryValidator.validate(spec);

        // Assert
        assertThat(errors).hasSize(4);
        
        assertThat(errors).anyMatch(e -> "transaction".equals(e.getEntity()) && "nonSelectableField".equals(e.getField()) && e.getMessage().contains("selectable"));
        assertThat(errors).anyMatch(e -> "unknown_entity".equals(e.getEntity()) && e.getMessage().contains("Entity unknown_entity does not exist"));
        assertThat(errors).anyMatch(e -> "transaction".equals(e.getEntity()) && "status".equals(e.getField()) && e.getMessage().contains("valid for string fields"));
        assertThat(errors).anyMatch(e -> "transaction".equals(e.getEntity()) && "unknown_field".equals(e.getField()) && e.getMessage().contains("does not exist"));
    }

    @Test
    void should_return_error_for_non_filterable_field() {
        // Arrange
        FilterCondition f1 = FilterCondition.builder()
                .entity("transaction")
                .field("instrumentId")
                .comparator("equals")
                .value("123")
                .build();
        
        FilterGroup group = FilterGroup.builder()
                .operator("AND")
                .conditions(Arrays.asList(f1))
                .build();

        QuerySpecification spec = QuerySpecification.builder()
                .filterBaseNode(group)
                .build();

        when(metadataService.findEntityByAlias("transaction")).thenReturn(
                EntityMetadata.builder().logicalName("transaction").build());
        when(metadataService.findField("transaction", "instrumentId")).thenReturn(
                FieldMetadata.builder().logicalName("instrumentId").dataType("number").isFilterable(false).build());

        // Act
        List<ValidationError> errors = queryValidator.validate(spec);

        // Assert
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0).getEntity()).isEqualTo("transaction");
        assertThat(errors.get(0).getField()).isEqualTo("instrumentId");
        assertThat(errors.get(0).getMessage()).contains("filterable");
    }
}
