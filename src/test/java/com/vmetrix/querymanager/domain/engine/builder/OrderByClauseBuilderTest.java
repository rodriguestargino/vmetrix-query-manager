package com.vmetrix.querymanager.domain.engine.builder;

import com.vmetrix.querymanager.domain.model.SortField;
import com.vmetrix.querymanager.domain.model.SortDirection;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.BiFunction;

import static org.assertj.core.api.Assertions.assertThat;

class OrderByClauseBuilderTest {

    private final OrderByClauseBuilder builder = new OrderByClauseBuilder();
    
    private final BiFunction<String, String, String> mockResolver = (entity, field) -> {
        if ("transaction".equals(entity)) return "t." + field.toUpperCase();
        return "alias." + field.toUpperCase();
    };

    @Test
    void should_build_order_by_clause() {
        List<SortField> fields = List.of(
                new SortField("transaction", "txnDate", SortDirection.DESC),
                new SortField("transaction", "txnId", SortDirection.ASC)
        );

        String result = builder.build(fields, mockResolver);
        assertThat(result).isEqualTo("ORDER BY t.TXNDATE DESC, t.TXNID ASC");
    }

    @Test
    void should_return_empty_when_null() {
        assertThat(builder.build(null, mockResolver)).isEmpty();
    }
}
