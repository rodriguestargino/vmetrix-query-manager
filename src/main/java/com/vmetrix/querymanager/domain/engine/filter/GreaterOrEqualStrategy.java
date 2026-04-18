package com.vmetrix.querymanager.domain.engine.filter;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/** Strategy: {@code col >= :pN} — 1 bind parameter */
public class GreaterOrEqualStrategy implements ComparatorStrategy {

    @Override
    public FilterResult apply(String qualifiedColumn, AtomicInteger paramCounter, Object value) {
        String paramName = "p" + paramCounter.getAndIncrement();
        return FilterResult.builder()
                .sqlFragment(qualifiedColumn + " >= :" + paramName)
                .parameters(Map.of(paramName, value))
                .build();
    }
}
