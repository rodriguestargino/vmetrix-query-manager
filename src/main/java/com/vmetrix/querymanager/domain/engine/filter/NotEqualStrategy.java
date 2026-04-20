package com.vmetrix.querymanager.domain.engine.filter;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

/** Strategy: {@code col <> :pN} — 1 bind parameter */
public class NotEqualStrategy implements ComparatorStrategy {

    @Override
    public FilterResult apply(String qualifiedColumn, AtomicInteger paramCounter, Object value) {
        String paramName = "p" + paramCounter.getAndIncrement();
        return FilterResult.builder()
                .sqlFragment(qualifiedColumn + " <> :" + paramName)
                .parameters(Collections.singletonMap(paramName, value))
                .build();
    }
}
