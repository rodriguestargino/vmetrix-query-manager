package com.vmetrix.querymanager.domain.model;

import lombok.Builder;
import lombok.Value;

import java.util.List;

/**
 * Composite node in the filter tree — holds an operator (AND/OR) and
 * a list of child {@link FilterNode} elements (which can be groups or conditions).
 */
@Value
@Builder
public class FilterGroup implements FilterNode {

    /** Boolean operator: "AND" or "OR" */
    String operator;

    /** Child nodes — can be FilterGroup (nested) or FilterCondition (leaf) */
    List<FilterNode> conditions;
}
