package com.vmetrix.querymanager.api.documentation;

/**
 * Centralized constants for OpenAPI examples.
 * Keeps controller classes thin and improves documentation maintainability.
 */
public final class OpenApiExamples {

    private OpenApiExamples() {
        // Utility class
    }

    public static final String BUILD_REQUEST_EXAMPLE =
        "{\n" +
            "  \"select\": [\n" +
            "    { \"entity\": \"transaction\", \"field\": \"txnDate\" },\n" +
            "    { \"entity\": \"counterparty\", \"field\": \"partyName\", \"alias\": \"counterpartyName\" }\n" +
            "  ],\n" +
            "  \"filters\": {\n" +
            "    \"operator\": \"AND\",\n" +
            "    \"conditions\": [\n" +
            "      { \"entity\": \"transaction\", \"field\": \"status\",\n" +
            "        \"comparator\": \"equals\", \"value\": \"SETTLED\" }\n" +
            "    ]\n" +
            "  },\n" +
            "  \"sorting\": [\n" +
            "    { \"entity\": \"transaction\", \"field\": \"txnDate\", \"direction\": \"DESC\" }\n" +
            "  ],\n" +
            "  \"maxResults\": 500\n" +
            "}";

    public static final String BUILD_RESPONSE_EXAMPLE =
        "{\n" +
            "  \"sql\": \"SELECT t.TXN_DATE, cp.PARTY_NAME AS counterpartyName FROM TRANSACTION t LEFT JOIN PARTY cp ON t.COUNTERPARTY_ID = cp.PARTY_ID WHERE t.STATUS = :p1 ORDER BY t.TXN_DATE DESC FETCH FIRST 500 ROWS ONLY\",\n" +
            "  \"parameters\": { \"p1\": \"SETTLED\" },\n" +
            "  \"resolvedTables\": [\"TRANSACTION\", \"PARTY\"],\n" +
            "  \"resolvedJoins\": [\"LEFT JOIN PARTY cp ON t.COUNTERPARTY_ID = cp.PARTY_ID\"],\n" +
            "  \"metadata\": {\n" +
            "    \"columnCount\": 2,\n" +
            "    \"filterCount\": 1,\n" +
            "    \"generatedAt\": \"2026-04-18T00:00:00Z\"\n" +
            "  }\n" +
            "}";

    public static final String VALIDATE_VALID_RESPONSE_EXAMPLE =
        "{\n" +
            "  \"valid\": true,\n" +
            "  \"errors\": []\n" +
            "}";

    public static final String VALIDATE_INVALID_RESPONSE_EXAMPLE =
        "{\n" +
            "  \"valid\": false,\n" +
            "  \"errors\": [\n" +
            "    {\n" +
            "      \"entity\": \"transaction\",\n" +
            "      \"field\": \"status\",\n" +
            "      \"comparator\": \"greaterThan\",\n" +
            "      \"message\": \"Comparator 'greaterThan' is not valid for string fields\"\n" +
            "    }\n" +
            "  ]\n" +
            "}";

    public static final String EXECUTE_RESPONSE_EXAMPLE =
        "[\n" +
            "  {\n" +
            "    \"txnId\": 1001,\n" +
            "    \"txnDate\": \"2026-04-18\",\n" +
            "    \"counterpartyName\": \"Global Bank corp\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"txnId\": 1002,\n" +
            "    \"txnDate\": \"2026-04-19\",\n" +
            "    \"counterpartyName\": \"Alpha Investments\"\n" +
            "  }\n" +
            "]";

    public static final String ERROR_RESPONSE_EXAMPLE =
        "{\n" +
            "  \"timestamp\": \"2026-04-18T00:00:00Z\",\n" +
            "  \"status\": 400,\n" +
            "  \"error\": \"Bad Request\",\n" +
            "  \"message\": \"Entity unknown_entity does not exist in metadata\"\n" +
            "}";

    public static final String COMPARATORS_RESPONSE_EXAMPLE =
        "{\n" +
            "  \"string\":    [\"equals\",\"notEquals\",\"like\",\"in\",\"notIn\",\"isNull\",\"isNotNull\"],\n" +
            "  \"number\":    [\"equals\",\"notEquals\",\"greaterThan\",\"lessThan\",\"greaterOrEqual\",\"lessOrEqual\",\"between\",\"in\",\"isNull\",\"isNotNull\"],\n" +
            "  \"date\":      [\"equals\",\"notEquals\",\"greaterThan\",\"lessThan\",\"greaterOrEqual\",\"lessOrEqual\",\"between\",\"isNull\",\"isNotNull\"],\n" +
            "  \"timestamp\": [\"equals\",\"notEquals\",\"greaterThan\",\"lessThan\",\"greaterOrEqual\",\"lessOrEqual\",\"isNull\",\"isNotNull\"]\n" +
            "}";
}
