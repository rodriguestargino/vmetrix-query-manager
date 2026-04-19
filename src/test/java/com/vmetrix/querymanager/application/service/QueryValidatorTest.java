package com.vmetrix.querymanager.application.service;

import com.vmetrix.querymanager.api.dto.request.FilterConditionDto;
import com.vmetrix.querymanager.api.dto.request.FilterGroupDto;
import com.vmetrix.querymanager.api.dto.request.QueryRequestDto;
import com.vmetrix.querymanager.api.dto.request.SelectFieldDto;
import com.vmetrix.querymanager.api.dto.request.SortDirectionDto;
import com.vmetrix.querymanager.api.dto.request.SortFieldDto;
import com.vmetrix.querymanager.domain.model.EntityMetadata;
import com.vmetrix.querymanager.domain.model.FieldMetadata;
import com.vmetrix.querymanager.domain.model.ValidationError;
import com.vmetrix.querymanager.shared.exception.UnknownEntityException;
import com.vmetrix.querymanager.shared.exception.UnknownFieldException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QueryValidatorTest {

    @Mock
    private MetadataService metadataService;

    @InjectMocks
    private QueryValidatorImpl queryValidator;

    @BeforeEach
    void setUp() {
        when(metadataService.getComparators()).thenReturn(
                java.util.Map.of(
                        "string", Arrays.asList("equals", "notEquals"),
                        "number", Arrays.asList("equals", "greaterThan")
                )
        );
    }

    @Test
    void should_collect_multiple_errors_in_one_pass() {
        // Arrange
        QueryRequestDto req = new QueryRequestDto();
        
        SelectFieldDto s1 = new SelectFieldDto();
        s1.setEntity("transaction");
        s1.setField("nonSelectableField");
        
        SelectFieldDto s2 = new SelectFieldDto();
        s2.setEntity("unknown_entity");
        s2.setField("foo");
        
        req.setSelect(Arrays.asList(s1, s2));
        
        FilterConditionDto f1 = new FilterConditionDto();
        f1.setEntity("transaction");
        f1.setField("status");
        f1.setComparator("greaterThan"); // valid for number, but status is string
        f1.setValue("SETTLED");
        
        FilterGroupDto group = new FilterGroupDto();
        group.setOperator("AND");
        group.setConditions(Arrays.asList(f1));
        req.setFilters(group);

        SortFieldDto sort1 = new SortFieldDto();
        sort1.setEntity("transaction");
        sort1.setField("unknown_field");
        sort1.setDirection(SortDirectionDto.ASC);
        req.setSorting(Arrays.asList(sort1));

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
        List<ValidationError> errors = queryValidator.validate(req);

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
        QueryRequestDto req = new QueryRequestDto();
        
        FilterConditionDto f1 = new FilterConditionDto();
        f1.setEntity("transaction");
        f1.setField("instrumentId");
        f1.setComparator("equals");
        f1.setValue("123");
        
        FilterGroupDto group = new FilterGroupDto();
        group.setOperator("AND");
        group.setConditions(Arrays.asList(f1));
        req.setFilters(group);

        when(metadataService.findEntityByAlias("transaction")).thenReturn(
                EntityMetadata.builder().logicalName("transaction").build());
        when(metadataService.findField("transaction", "instrumentId")).thenReturn(
                FieldMetadata.builder().logicalName("instrumentId").dataType("number").isFilterable(false).build());

        // Act
        List<ValidationError> errors = queryValidator.validate(req);

        // Assert
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0).getEntity()).isEqualTo("transaction");
        assertThat(errors.get(0).getField()).isEqualTo("instrumentId");
        assertThat(errors.get(0).getMessage()).contains("filterable");
    }
}
