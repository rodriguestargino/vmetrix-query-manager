package com.vmetrix.querymanager.domain.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RelationshipMetadata {
    String sourceEntity;
    String sourceColumn;
    String targetEntity;
    String targetColumn;
    String joinType;
    String relationAlias;
}
