package com.vmetrix.querymanager.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Response returned by POST /api/query/build — the generated SQL plus "
        + "the resolved bind parameters, tables, and JOINs.")
public class QueryBuildResponse {

    @Schema(description = "Generated parameterized SQL. Bind placeholders use named parameters "
            + "(e.g. :p1). Never contains concatenated literal values.",
            example = "SELECT t.TXN_DATE, cp.PARTY_NAME AS counterpartyName FROM TRANSACTION t "
                    + "LEFT JOIN PARTY cp ON t.COUNTERPARTY_ID = cp.PARTY_ID "
                    + "WHERE t.STATUS = :p1 FETCH FIRST 500 ROWS ONLY")
    private String sql;

    @Schema(description = "Named bind parameters referenced by the generated SQL.",
            example = "{\"p1\": \"SETTLED\"}")
    private Map<String, Object> parameters;

    @Schema(description = "Physical table names that appear in the generated FROM/JOIN clauses.",
            example = "[\"TRANSACTION\", \"PARTY\"]")
    private List<String> resolvedTables;

    @Schema(description = "Resolved JOIN clauses in the exact order emitted by the engine.",
            example = "[\"LEFT JOIN PARTY cp ON t.COUNTERPARTY_ID = cp.PARTY_ID\"]")
    private List<String> resolvedJoins;

    @Schema(description = "Extra metadata about the generated query.")
    private QueryMetadataDto metadata;
}
