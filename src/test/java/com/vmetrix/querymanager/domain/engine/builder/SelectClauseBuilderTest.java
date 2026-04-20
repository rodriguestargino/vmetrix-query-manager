package com.vmetrix.querymanager.domain.engine.builder;

import com.vmetrix.querymanager.domain.model.SelectField;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SelectClauseBuilderTest {

    private final SelectClauseBuilder builder = new SelectClauseBuilder();
    
    private final BiFunction<String, String, String> mockResolver = (entity, field) -> {
        if ("transaction".equals(entity)) return "t." + field.toUpperCase();
        if ("counterparty".equals(entity)) return "cp." + field.toUpperCase();
        return "alias." + field.toUpperCase();
    };

    @Test
    void should_build_select_clause() {
        List<SelectField> fields = Arrays.asList(
                new SelectField("transaction", "txnId", null),
                new SelectField("counterparty", "partyName", "counterpartyName")
        );

        String result = builder.build(fields, mockResolver);
        assertThat(result).isEqualTo("SELECT t.TXNID, cp.PARTYNAME AS counterpartyName");
    }

    @Test
    void should_throw_when_empty() {
        assertThatThrownBy(() -> builder.build(Collections.emptyList(), mockResolver))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
