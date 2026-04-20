package com.vmetrix.querymanager.domain.engine.filter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TDD unit tests for individual ComparatorStrategy implementations.
 * Pure domain tests: no Spring context.
 */
class ComparatorStrategyTest {

    // ── Test 1: equals on string field ──

    @Test
    @DisplayName("should generate equals SQL with 1 bind param")
    void should_generate_equals_sql_with_one_bind_param() {
        ComparatorStrategy strategy = new EqualStrategy();
        AtomicInteger counter = new AtomicInteger(1);

        FilterResult result = strategy.apply("t.STATUS", counter, "SETTLED");

        assertEquals("t.STATUS = :p1", result.getSqlFragment());
        assertEquals(Collections.singletonMap("p1", "SETTLED"), result.getParameters());
        assertEquals(2, counter.get(), "Counter should be incremented to 2");
    }

    // ── Test 2: greaterThan on number field ──

    @Test
    @DisplayName("should generate greaterThan SQL with 1 bind param")
    void should_generate_greater_than_sql_with_one_bind_param() {
        ComparatorStrategy strategy = new GreaterThanStrategy();
        AtomicInteger counter = new AtomicInteger(1);

        FilterResult result = strategy.apply("t.AMOUNT", counter, 1000000);

        assertEquals("t.AMOUNT > :p1", result.getSqlFragment());
        assertEquals(Collections.singletonMap("p1", 1000000), result.getParameters());
    }

    // ── Test 3: in on string field with list ──

    @Test
    @DisplayName("should generate IN SQL with N bind params")
    void should_generate_in_sql_with_n_bind_params() {
        ComparatorStrategy strategy = new InStrategy();
        AtomicInteger counter = new AtomicInteger(1);

        FilterResult result = strategy.apply("i.ASSET_CLASS", counter, Arrays.asList("EQUITY", "FIXED_INCOME", "ALTERNATIVES"));

        assertEquals("i.ASSET_CLASS IN (:p1, :p2, :p3)", result.getSqlFragment());
        assertEquals(3, result.getParameters().size());
        assertEquals("EQUITY", result.getParameters().get("p1"));
        assertEquals("FIXED_INCOME", result.getParameters().get("p2"));
        assertEquals("ALTERNATIVES", result.getParameters().get("p3"));
        assertEquals(4, counter.get(), "Counter should advance by 3");
    }

    // ── Test 4: between on date field ──

    @Test
    @DisplayName("should generate BETWEEN SQL with 2 bind params")
    void should_generate_between_sql_with_two_bind_params() {
        ComparatorStrategy strategy = new BetweenStrategy();
        AtomicInteger counter = new AtomicInteger(1);

        FilterResult result = strategy.apply("t.TXN_DATE", counter, Arrays.asList("2026-01-01", "2026-12-31"));

        assertEquals("t.TXN_DATE BETWEEN :p1 AND :p2", result.getSqlFragment());
        assertEquals(2, result.getParameters().size());
        assertEquals("2026-01-01", result.getParameters().get("p1"));
        assertEquals("2026-12-31", result.getParameters().get("p2"));
        assertEquals(3, counter.get(), "Counter should advance by 2");
    }

    // ── Test 5: isNull ──

    @Test
    @DisplayName("should generate IS NULL SQL with 0 bind params")
    void should_generate_is_null_sql_with_zero_bind_params() {
        ComparatorStrategy strategy = new IsNullStrategy();
        AtomicInteger counter = new AtomicInteger(1);

        FilterResult result = strategy.apply("t.SETTLEMENT_DATE", counter, null);

        assertEquals("t.SETTLEMENT_DATE IS NULL", result.getSqlFragment());
        assertTrue(result.getParameters().isEmpty());
        assertEquals(1, counter.get(), "Counter should NOT be incremented");
    }

    // ── Test 6: isNotNull ──

    @Test
    @DisplayName("should generate IS NOT NULL SQL with 0 bind params")
    void should_generate_is_not_null_sql_with_zero_bind_params() {
        ComparatorStrategy strategy = new IsNotNullStrategy();
        AtomicInteger counter = new AtomicInteger(1);

        FilterResult result = strategy.apply("t.SETTLEMENT_DATE", counter, null);

        assertEquals("t.SETTLEMENT_DATE IS NOT NULL", result.getSqlFragment());
        assertTrue(result.getParameters().isEmpty());
        assertEquals(1, counter.get(), "Counter should NOT be incremented");
    }

    // ── Test 7: like on string field ──

    @Test
    @DisplayName("should generate LIKE SQL with 1 bind param")
    void should_generate_like_sql_with_one_bind_param() {
        ComparatorStrategy strategy = new LikeStrategy();
        AtomicInteger counter = new AtomicInteger(1);

        FilterResult result = strategy.apply("p.PARTY_NAME", counter, "%Bank%");

        assertEquals("p.PARTY_NAME LIKE :p1", result.getSqlFragment());
        assertEquals(Collections.singletonMap("p1", "%Bank%"), result.getParameters());
    }
}
