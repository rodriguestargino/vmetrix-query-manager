package com.vmetrix.querymanager.domain.engine.filter;

import com.vmetrix.querymanager.shared.exception.InvalidComparatorException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Factory that resolves the correct {@link ComparatorStrategy} at runtime by comparator name.
 * Pure domain class — no Spring annotations.
 *
 * <p>All 12 comparator strategies are registered at construction time.</p>
 */
public class ComparatorStrategyFactory {

    private final Map<String, ComparatorStrategy> strategies;

    public ComparatorStrategyFactory() {
        Map<String, ComparatorStrategy> map = new HashMap<>();
        map.put("equals", new EqualStrategy());
        map.put("notEquals", new NotEqualStrategy());
        map.put("greaterThan", new GreaterThanStrategy());
        map.put("lessThan", new LessThanStrategy());
        map.put("greaterOrEqual", new GreaterOrEqualStrategy());
        map.put("lessOrEqual", new LessOrEqualStrategy());
        map.put("in", new InStrategy());
        map.put("notIn", new NotInStrategy());
        map.put("between", new BetweenStrategy());
        map.put("like", new LikeStrategy());
        map.put("isNull", new IsNullStrategy());
        map.put("isNotNull", new IsNotNullStrategy());
        this.strategies = Collections.unmodifiableMap(map);
    }

    /**
     * Resolves the strategy for the given comparator name.
     *
     * @param comparatorName the comparator (e.g. "equals", "between", "isNull")
     * @return the corresponding ComparatorStrategy
     * @throws InvalidComparatorException if the comparator name is unknown
     */
    public ComparatorStrategy getStrategy(String comparatorName) {
        ComparatorStrategy strategy = strategies.get(comparatorName);
        if (strategy == null) {
            throw new InvalidComparatorException(comparatorName);
        }
        return strategy;
    }
}
