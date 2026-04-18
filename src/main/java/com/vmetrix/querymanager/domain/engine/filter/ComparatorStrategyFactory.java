package com.vmetrix.querymanager.domain.engine.filter;

import com.vmetrix.querymanager.shared.exception.InvalidComparatorException;

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
        this.strategies = Map.ofEntries(
                Map.entry("equals", new EqualStrategy()),
                Map.entry("notEquals", new NotEqualStrategy()),
                Map.entry("greaterThan", new GreaterThanStrategy()),
                Map.entry("lessThan", new LessThanStrategy()),
                Map.entry("greaterOrEqual", new GreaterOrEqualStrategy()),
                Map.entry("lessOrEqual", new LessOrEqualStrategy()),
                Map.entry("in", new InStrategy()),
                Map.entry("notIn", new NotInStrategy()),
                Map.entry("between", new BetweenStrategy()),
                Map.entry("like", new LikeStrategy()),
                Map.entry("isNull", new IsNullStrategy()),
                Map.entry("isNotNull", new IsNotNullStrategy())
        );
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
