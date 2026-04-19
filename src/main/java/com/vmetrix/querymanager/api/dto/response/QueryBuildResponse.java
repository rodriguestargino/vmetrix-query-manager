package com.vmetrix.querymanager.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryBuildResponse {
    private String sql;
    private Map<String, Object> parameters;
    private List<String> resolvedTables;
    private List<String> resolvedJoins;
    private QueryMetadataDto metadata;
}
