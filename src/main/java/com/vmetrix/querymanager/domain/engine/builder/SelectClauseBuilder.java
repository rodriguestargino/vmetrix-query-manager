package com.vmetrix.querymanager.domain.engine.builder;

import com.vmetrix.querymanager.domain.model.SelectField;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class SelectClauseBuilder {

    public String build(List<SelectField> fields, BiFunction<String, String, String> columnResolver) {
        if (fields == null || fields.isEmpty()) {
            throw new IllegalArgumentException("SELECT fields cannot be empty");
        }

        String columns = fields.stream()
                .map(f -> buildColumn(f, columnResolver))
                .collect(Collectors.joining(", "));

        return "SELECT " + columns;
    }

    private String buildColumn(SelectField field, BiFunction<String, String, String> columnResolver) {
        String baseColumn = columnResolver.apply(field.getEntity(), field.getField());
        if (field.getAlias() != null && !field.getAlias().isBlank()) {
            return baseColumn + " AS " + field.getAlias();
        }
        return baseColumn;
    }
}
