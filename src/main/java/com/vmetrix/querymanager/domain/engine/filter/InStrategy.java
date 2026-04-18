package com.vmetrix.querymanager.domain.engine.filter;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/** Strategy: {@code col IN (:p1, :p2, ...)} — N bind parameters, one per list element */
public class InStrategy implements ComparatorStrategy {

    @Override
    @SuppressWarnings("unchecked")
    public FilterResult apply(String qualifiedColumn, AtomicInteger paramCounter, Object value) {
        List<?> values = (List<?>) value;
        Map<String, Object> params = new LinkedHashMap<>();
        List<String> placeholders = new ArrayList<>();

        for (Object v : values) {
            String paramName = "p" + paramCounter.getAndIncrement();
            params.put(paramName, v);
            placeholders.add(":" + paramName);
        }

        String sql = qualifiedColumn + " IN (" + String.join(", ", placeholders) + ")";
        return FilterResult.builder()
                .sqlFragment(sql)
                .parameters(params)
                .build();
    }
}
