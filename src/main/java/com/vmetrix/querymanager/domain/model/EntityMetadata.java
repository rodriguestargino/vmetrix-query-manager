package com.vmetrix.querymanager.domain.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class EntityMetadata {
    String logicalName;
    String physicalName;
    String defaultAlias;
    String description;
}
