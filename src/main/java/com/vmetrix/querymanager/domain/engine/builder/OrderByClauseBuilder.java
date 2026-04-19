package com.vmetrix.querymanager.domain.engine.builder;

import com.vmetrix.querymanager.domain.model.SortField;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class OrderByClauseBuilder {

    public String build(List<SortField> sortFields, BiFunction<String, String, String> columnResolver) {
        if (sortFields == null || sortFields.isEmpty()) {
            return "";
        }

        String orderBy = sortFields.stream()
                .map(f -> buildSort(f, columnResolver))
                .collect(Collectors.joining(", "));

        return "ORDER BY " + orderBy;
    }

    private String buildSort(SortField sortField, BiFunction<String, String, String> columnResolver) {
        String baseColumn = columnResolver.apply(sortField.getEntity(), sortField.getField());
        return baseColumn + " " + sortField.getDirection().name();
    }
}
