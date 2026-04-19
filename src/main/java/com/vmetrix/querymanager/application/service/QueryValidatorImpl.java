package com.vmetrix.querymanager.application.service;

import com.vmetrix.querymanager.domain.engine.builder.QuerySpecification;
import com.vmetrix.querymanager.domain.model.*;
import com.vmetrix.querymanager.domain.port.MetadataCatalog;
import com.vmetrix.querymanager.shared.exception.UnknownEntityException;
import com.vmetrix.querymanager.shared.exception.UnknownFieldException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class QueryValidatorImpl implements QueryValidator {

    private final MetadataCatalog metadataService;

    @Override
    public List<ValidationError> validate(QuerySpecification spec) {
        List<ValidationError> errors = new ArrayList<>();
        Map<String, List<String>> comparators = metadataService.getComparators();

        // Validate selects
        if (spec.getSelectFields() != null) {
            for (SelectField select : spec.getSelectFields()) {
                try {
                    EntityMetadata em = metadataService.findEntityByAlias(select.getEntity());
                    FieldMetadata fieldMeta = metadataService.findField(em.getLogicalName(), select.getField());
                    if (!fieldMeta.isSelectable()) {
                        errors.add(ValidationError.builder()
                                .entity(select.getEntity())
                                .field(select.getField())
                                .message("Field " + select.getField() + " is not selectable")
                                .build());
                    }
                } catch (UnknownEntityException e) {
                    errors.add(ValidationError.builder()
                            .entity(select.getEntity())
                            .message("Entity " + select.getEntity() + " does not exist in the model")
                            .build());
                } catch (UnknownFieldException e) {
                    errors.add(ValidationError.builder()
                            .entity(select.getEntity())
                            .field(select.getField())
                            .message("Field " + select.getField() + " does not exist in entity " + select.getEntity())
                            .build());
                }
            }
        }

        // Validate filters
        if (spec.getFilterBaseNode() != null) {
            validateFilterNode(spec.getFilterBaseNode(), errors, comparators);
        }

        // Validate sorting
        if (spec.getSortFields() != null) {
            for (SortField sort : spec.getSortFields()) {
                try {
                    EntityMetadata em = metadataService.findEntityByAlias(sort.getEntity());
                    metadataService.findField(em.getLogicalName(), sort.getField());
                } catch (UnknownEntityException e) {
                    errors.add(ValidationError.builder()
                            .entity(sort.getEntity())
                            .message("Entity " + sort.getEntity() + " does not exist in the model")
                            .build());
                } catch (UnknownFieldException e) {
                    errors.add(ValidationError.builder()
                            .entity(sort.getEntity())
                            .field(sort.getField())
                            .message("Field " + sort.getField() + " does not exist in entity " + sort.getEntity())
                            .build());
                }
            }
        }

        return errors;
    }

    private void validateFilterNode(FilterNode node, List<ValidationError> errors, Map<String, List<String>> comparators) {
        if (node instanceof FilterGroup group) {
            if (group.getConditions() != null) {
                for (FilterNode child : group.getConditions()) {
                    validateFilterNode(child, errors, comparators);
                }
            }
        } else if (node instanceof FilterCondition condition) {
            try {
                EntityMetadata em = metadataService.findEntityByAlias(condition.getEntity());
                FieldMetadata fieldMeta = metadataService.findField(em.getLogicalName(), condition.getField());
                if (!fieldMeta.isFilterable()) {
                    errors.add(ValidationError.builder()
                            .entity(condition.getEntity())
                            .field(condition.getField())
                            .comparator(condition.getComparator())
                            .message("Field " + condition.getField() + " is not filterable")
                            .build());
                } else if (condition.getComparator() != null) {
                    List<String> validComparators = comparators.get(fieldMeta.getDataType());
                    if (validComparators == null || !validComparators.contains(condition.getComparator())) {
                        errors.add(ValidationError.builder()
                                .entity(condition.getEntity())
                                .field(condition.getField())
                                .comparator(condition.getComparator())
                                .message("Comparator " + condition.getComparator() + " is not valid for " + fieldMeta.getDataType() + " fields")
                                .build());
                    }
                }
            } catch (UnknownEntityException e) {
                errors.add(ValidationError.builder()
                        .entity(condition.getEntity())
                        .message("Entity " + condition.getEntity() + " does not exist in the model")
                        .build());
            } catch (UnknownFieldException e) {
                errors.add(ValidationError.builder()
                        .entity(condition.getEntity())
                        .field(condition.getField())
                        .message("Field " + condition.getField() + " does not exist in entity " + condition.getEntity())
                        .build());
            }
        }
    }
}
