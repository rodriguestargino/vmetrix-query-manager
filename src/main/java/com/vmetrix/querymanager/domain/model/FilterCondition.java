package com.vmetrix.querymanager.domain.model;

import lombok.Builder;
import lombok.Value;

/**
 * Leaf node in the filter tree — represents a single filter condition
 * with logical entity/field names, a comparator, and a value.
 *
 * <p>Resolution to physical column names (alias.PHYSICAL_COLUMN) happens
 * at render time via MetadataService, NOT at parse time.</p>
 */
@Value
@Builder
public class FilterCondition implements FilterNode {

    /** Logical entity name (e.g. "transaction", "counterparty") */
    String entity;

    /** Logical field name (e.g. "status", "txnDate") */
    String field;

    /** Comparator name (e.g. "equals", "between", "in", "isNull") */
    String comparator;

    /** Filter value — single value, List for in/notIn/between, or null for isNull/isNotNull */
    Object value;
}
