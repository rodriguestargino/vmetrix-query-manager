package com.vmetrix.querymanager.domain.engine.filter;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

/** Strategy: {@code col IS NOT NULL} — 0 bind parameters, no value consumed */
public class IsNotNullStrategy implements ComparatorStrategy {

    @Override
    public FilterResult apply(String qualifiedColumn, AtomicInteger paramCounter, Object value) {
        return FilterResult.builder()
                .sqlFragment(qualifiedColumn + " IS NOT NULL")
                .parameters(Collections.emptyMap())
                .build();
    }
}
