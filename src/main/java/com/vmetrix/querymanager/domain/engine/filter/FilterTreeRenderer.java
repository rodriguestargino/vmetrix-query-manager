package com.vmetrix.querymanager.domain.engine.filter;

import com.vmetrix.querymanager.domain.model.FilterCondition;
import com.vmetrix.querymanager.domain.model.FilterGroup;
import com.vmetrix.querymanager.domain.model.FilterNode;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * Recursively traverses a {@link FilterNode} tree (Composite pattern) and produces
 * a parameterized SQL WHERE clause with bind parameters.
 *
 * <p>This is a pure domain class — no Spring annotations, no JPA imports.
 * Column resolution is provided via a {@link BiFunction} parameter.</p>
 *
 * <h3>Key rules:</h3>
 * <ul>
 *   <li>Parameter counter is global per query invocation (p1, p2, p3...)</li>
 *   <li>Values are NEVER concatenated into SQL — always bind parameters</li>
 *   <li>Groups are parenthesized: {@code (condition1 AND condition2)}</li>
 * </ul>
 */
public class FilterTreeRenderer {

    private final ComparatorStrategyFactory strategyFactory;

    public FilterTreeRenderer(ComparatorStrategyFactory strategyFactory) {
        this.strategyFactory = strategyFactory;
    }

    /**
     * Renders a filter tree into a SQL WHERE clause with bind parameters.
     *
     * @param root            the root FilterNode (FilterGroup or FilterCondition)
     * @param columnResolver  resolves (entity, field) → "alias.PHYSICAL_COLUMN"
     * @param paramCounter    global parameter counter shared across the entire query
     * @return FilterResult with complete WHERE clause SQL and merged parameter map
     */
    public FilterResult render(FilterNode root,
                               BiFunction<String, String, String> columnResolver,
                               AtomicInteger paramCounter) {
        if (root instanceof FilterCondition) {
            return renderCondition((FilterCondition) root, columnResolver, paramCounter);
        } else if (root instanceof FilterGroup) {
            return renderGroup((FilterGroup) root, columnResolver, paramCounter);
        }
        throw new IllegalArgumentException("Unknown FilterNode type: " + root.getClass().getName());
    }

    private FilterResult renderCondition(FilterCondition condition,
                                         BiFunction<String, String, String> columnResolver,
                                         AtomicInteger paramCounter) {
        String qualifiedColumn = columnResolver.apply(condition.getEntity(), condition.getField());
        ComparatorStrategy strategy = strategyFactory.getStrategy(condition.getComparator());
        return strategy.apply(qualifiedColumn, paramCounter, condition.getValue());
    }

    private FilterResult renderGroup(FilterGroup group,
                                     BiFunction<String, String, String> columnResolver,
                                     AtomicInteger paramCounter) {
        List<String> fragments = new ArrayList<>();
        Map<String, Object> allParams = new LinkedHashMap<>();

        for (FilterNode child : group.getConditions()) {
            FilterResult childResult = render(child, columnResolver, paramCounter);
            fragments.add(childResult.getSqlFragment());
            allParams.putAll(childResult.getParameters());
        }

        String joiner = " " + group.getOperator() + " ";
        String sql = "(" + String.join(joiner, fragments) + ")";

        return FilterResult.builder()
                .sqlFragment(sql)
                .parameters(allParams)
                .build();
    }
}
