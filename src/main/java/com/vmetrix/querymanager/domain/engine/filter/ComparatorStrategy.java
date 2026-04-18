package com.vmetrix.querymanager.domain.engine.filter;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Strategy interface for comparator-specific SQL generation.
 * Each implementation produces a SQL fragment and bind parameters
 * for a single comparator type (equals, between, in, isNull, etc.).
 */
public interface ComparatorStrategy {

    /**
     * Generates a SQL fragment and bind parameters for this comparator.
     *
     * @param qualifiedColumn fully qualified column (e.g. "t.STATUS")
     * @param paramCounter    global parameter counter shared across the entire query
     * @param value           the filter value (single value, list, or null)
     * @return FilterResult with SQL fragment and parameter map
     */
    FilterResult apply(String qualifiedColumn, AtomicInteger paramCounter, Object value);
}
