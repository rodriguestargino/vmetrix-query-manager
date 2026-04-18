package com.vmetrix.querymanager.domain.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class FieldMetadata {
    String logicalName;
    String physicalName;
    String entityLogicalName;
    String dataType;
    boolean isPk;
    boolean isFk;
    String fkEntity; // FK target logical entity
    String fkColumn; // FK target logical column
    boolean isFilterable;
    boolean isSelectable;
}
