package com.vmetrix.querymanager.domain.engine.filter;

import com.vmetrix.querymanager.shared.exception.InvalidComparatorException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TDD unit tests for ComparatorStrategyFactory.
 * Pure domain tests: no Spring context.
 */
class ComparatorStrategyFactoryTest {

    private ComparatorStrategyFactory factory;

    @BeforeEach
    void setUp() {
        factory = new ComparatorStrategyFactory();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "equals", "notEquals", "greaterThan", "lessThan",
            "greaterOrEqual", "lessOrEqual", "in", "notIn",
            "between", "like", "isNull", "isNotNull"
    })
    @DisplayName("should resolve all 12 valid comparators")
    void should_resolve_all_valid_comparators(String comparatorName) {
        ComparatorStrategy strategy = factory.getStrategy(comparatorName);
        assertNotNull(strategy, "Strategy for '" + comparatorName + "' should not be null");
    }

    @Test
    @DisplayName("should throw InvalidComparatorException for unknown comparator")
    void should_throw_exception_for_unknown_comparator() {
        InvalidComparatorException ex = assertThrows(
                InvalidComparatorException.class,
                () -> factory.getStrategy("unknownComparator")
        );
        assertTrue(ex.getMessage().contains("unknownComparator"));
    }
}
