package com.vmetrix.querymanager.domain.engine.filter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/** Strategy: {@code col BETWEEN :pN AND :pN+1} — exactly 2 bind parameters */
public class BetweenStrategy implements ComparatorStrategy {

    @Override
    @SuppressWarnings("unchecked")
    public FilterResult apply(String qualifiedColumn, AtomicInteger paramCounter, Object value) {
        List<?> range = (List<?>) value;
        String paramLow = "p" + paramCounter.getAndIncrement();
        String paramHigh = "p" + paramCounter.getAndIncrement();

        Map<String, Object> params = new LinkedHashMap<>();
        params.put(paramLow, range.get(0));
        params.put(paramHigh, range.get(1));

        String sql = qualifiedColumn + " BETWEEN :" + paramLow + " AND :" + paramHigh;
        return FilterResult.builder()
                .sqlFragment(sql)
                .parameters(params)
                .build();
    }
}
