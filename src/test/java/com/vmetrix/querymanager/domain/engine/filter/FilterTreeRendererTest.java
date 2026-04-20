package com.vmetrix.querymanager.domain.engine.filter;

import com.vmetrix.querymanager.domain.model.FilterCondition;
import com.vmetrix.querymanager.domain.model.FilterGroup;
import com.vmetrix.querymanager.domain.model.FilterNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TDD unit tests for FilterTreeRenderer — recursive Composite tree to SQL.
 * Pure domain tests: no Spring context.
 */
class FilterTreeRendererTest {

    private FilterTreeRenderer renderer;
    private BiFunction<String, String, String> columnResolver;

    @BeforeEach
    void setUp() {
        renderer = new FilterTreeRenderer(new ComparatorStrategyFactory());

        // Simple column resolver for testing: maps entity+field to alias.COLUMN
        columnResolver = (entity, field) -> {
            switch (entity + "." + field) {
                case "transaction.status": return "t.STATUS";
                case "transaction.amount": return "t.AMOUNT";
                case "transaction.txnDate": return "t.TXN_DATE";
                case "instrument.assetClass": return "i.ASSET_CLASS";
                case "counterparty.country": return "counterparty.COUNTRY";
                case "counterparty.rating": return "counterparty.RATING";
                default: return entity + "." + field;
            }
        };
    }

    // ── Test 8: Flat AND group with 3 conditions ──

    @Test
    @DisplayName("should render flat AND group with correct parenthesization")
    void should_render_flat_and_group_with_correct_parenthesization() {
        FilterNode tree = FilterGroup.builder()
                .operator("AND")
                .conditions(Arrays.asList(
                        FilterCondition.builder()
                                .entity("transaction").field("status")
                                .comparator("equals").value("SETTLED").build(),
                        FilterCondition.builder()
                                .entity("transaction").field("amount")
                                .comparator("greaterThan").value(1000000).build(),
                        FilterCondition.builder()
                                .entity("transaction").field("txnDate")
                                .comparator("greaterThan").value("2026-01-01").build()
                ))
                .build();

        AtomicInteger counter = new AtomicInteger(1);
        FilterResult result = renderer.render(tree, columnResolver, counter);

        assertEquals("(t.STATUS = :p1 AND t.AMOUNT > :p2 AND t.TXN_DATE > :p3)", result.getSqlFragment());
        assertEquals(3, result.getParameters().size());
        assertEquals("SETTLED", result.getParameters().get("p1"));
        assertEquals(1000000, result.getParameters().get("p2"));
        assertEquals("2026-01-01", result.getParameters().get("p3"));
        assertEquals(4, counter.get());
    }

    // ── Test 9: Nested AND → OR (spec §5.1 example) ──

    @Test
    @DisplayName("should render nested AND-OR with exact SQL structure")
    void should_render_nested_and_or_with_exact_sql_structure() {
        // status = SETTLED AND amount > 1000000 AND (assetClass IN (...) OR country = CL)
        FilterNode tree = FilterGroup.builder()
                .operator("AND")
                .conditions(Arrays.asList(
                        FilterCondition.builder()
                                .entity("transaction").field("status")
                                .comparator("equals").value("SETTLED").build(),
                        FilterCondition.builder()
                                .entity("transaction").field("amount")
                                .comparator("greaterThan").value(1000000).build(),
                        FilterGroup.builder()
                                .operator("OR")
                                .conditions(Arrays.asList(
                                        FilterCondition.builder()
                                                .entity("instrument").field("assetClass")
                                                .comparator("in").value(Arrays.asList("EQUITY", "FIXED_INCOME")).build(),
                                        FilterCondition.builder()
                                                .entity("counterparty").field("country")
                                                .comparator("equals").value("CL").build()
                                ))
                                .build()
                ))
                .build();

        AtomicInteger counter = new AtomicInteger(1);
        FilterResult result = renderer.render(tree, columnResolver, counter);

        assertEquals(
                "(t.STATUS = :p1 AND t.AMOUNT > :p2 AND (i.ASSET_CLASS IN (:p3, :p4) OR counterparty.COUNTRY = :p5))",
                result.getSqlFragment()
        );
        assertEquals(5, result.getParameters().size());
        assertEquals("SETTLED", result.getParameters().get("p1"));
        assertEquals(1000000, result.getParameters().get("p2"));
        assertEquals("EQUITY", result.getParameters().get("p3"));
        assertEquals("FIXED_INCOME", result.getParameters().get("p4"));
        assertEquals("CL", result.getParameters().get("p5"));
    }

    // ── Test 10: 3-level deep nesting ──

    @Test
    @DisplayName("should render 3-level deep nesting with correct parenthesization")
    void should_render_three_level_deep_nesting_with_correct_parenthesization() {
        // AND(
        //   status = SETTLED,
        //   OR(
        //     amount > 1000000,
        //     AND(
        //       country = CL,
        //       rating = AA
        //     )
        //   )
        // )
        FilterNode tree = FilterGroup.builder()
                .operator("AND")
                .conditions(Arrays.asList(
                        FilterCondition.builder()
                                .entity("transaction").field("status")
                                .comparator("equals").value("SETTLED").build(),
                        FilterGroup.builder()
                                .operator("OR")
                                .conditions(Arrays.asList(
                                        FilterCondition.builder()
                                                .entity("transaction").field("amount")
                                                .comparator("greaterThan").value(1000000).build(),
                                        FilterGroup.builder()
                                                .operator("AND")
                                                .conditions(Arrays.asList(
                                                        FilterCondition.builder()
                                                                .entity("counterparty").field("country")
                                                                .comparator("equals").value("CL").build(),
                                                        FilterCondition.builder()
                                                                .entity("counterparty").field("rating")
                                                                .comparator("equals").value("AA").build()
                                                ))
                                                .build()
                                ))
                                .build()
                ))
                .build();

        AtomicInteger counter = new AtomicInteger(1);
        FilterResult result = renderer.render(tree, columnResolver, counter);

        assertEquals(
                "(t.STATUS = :p1 AND (t.AMOUNT > :p2 OR (counterparty.COUNTRY = :p3 AND counterparty.RATING = :p4)))",
                result.getSqlFragment()
        );
        assertEquals(4, result.getParameters().size());
        assertEquals("SETTLED", result.getParameters().get("p1"));
        assertEquals(1000000, result.getParameters().get("p2"));
        assertEquals("CL", result.getParameters().get("p3"));
        assertEquals("AA", result.getParameters().get("p4"));
    }
}
