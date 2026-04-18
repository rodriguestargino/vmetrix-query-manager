package com.vmetrix.querymanager.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryResult {
    private String sql;
    private Map<String, Object> parameters;
    private List<String> resolvedTables;
    private List<String> resolvedJoins;
    private int columnCount;
    private int filterCount;
    private Instant generatedAt;
}
