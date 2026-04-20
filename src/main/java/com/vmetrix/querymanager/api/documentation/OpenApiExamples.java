package com.vmetrix.querymanager.api.documentation;

/**
 * Centralized constants for OpenAPI examples.
 * Keeps controller classes thin and improves documentation maintainability.
 */
public final class OpenApiExamples {

    private OpenApiExamples() {
        // Utility class
    }

    public static final String BUILD_REQUEST_EXAMPLE = """
            {
              "select": [
                { "entity": "transaction", "field": "txnDate" },
                { "entity": "counterparty", "field": "partyName", "alias": "counterpartyName" }
              ],
              "filters": {
                "operator": "AND",
                "conditions": [
                  { "entity": "transaction", "field": "status",
                    "comparator": "equals", "value": "SETTLED" }
                ]
              },
              "sorting": [
                { "entity": "transaction", "field": "txnDate", "direction": "DESC" }
              ],
              "maxResults": 500
            }
            """;

    public static final String BUILD_RESPONSE_EXAMPLE = """
            {
              "sql": "SELECT t.TXN_DATE, cp.PARTY_NAME AS counterpartyName FROM TRANSACTION t LEFT JOIN PARTY cp ON t.COUNTERPARTY_ID = cp.PARTY_ID WHERE t.STATUS = :p1 ORDER BY t.TXN_DATE DESC FETCH FIRST 500 ROWS ONLY",
              "parameters": { "p1": "SETTLED" },
              "resolvedTables": ["TRANSACTION", "PARTY"],
              "resolvedJoins": ["LEFT JOIN PARTY cp ON t.COUNTERPARTY_ID = cp.PARTY_ID"],
              "metadata": {
                "columnCount": 2,
                "filterCount": 1,
                "generatedAt": "2026-04-18T00:00:00Z"
              }
            }
            """;

    public static final String VALIDATE_VALID_RESPONSE_EXAMPLE = """
            {
              "valid": true,
              "errors": []
            }
            """;

    public static final String VALIDATE_INVALID_RESPONSE_EXAMPLE = """
            {
              "valid": false,
              "errors": [
                {
                  "entity": "transaction",
                  "field": "status",
                  "comparator": "greaterThan",
                  "message": "Comparator 'greaterThan' is not valid for string fields"
                }
              ]
            }
            """;

    public static final String EXECUTE_RESPONSE_EXAMPLE = """
            [
              {
                "txnId": 1001,
                "txnDate": "2026-04-18",
                "counterpartyName": "Global Bank corp"
              },
              {
                "txnId": 1002,
                "txnDate": "2026-04-19",
                "counterpartyName": "Alpha Investments"
              }
            ]
            """;

    public static final String ERROR_RESPONSE_EXAMPLE = """
            {
              "timestamp": "2026-04-18T00:00:00Z",
              "status": 400,
              "error": "Bad Request",
              "message": "Entity unknown_entity does not exist in metadata"
            }
            """;

    public static final String COMPARATORS_RESPONSE_EXAMPLE = """
            {
              "string":    ["equals","notEquals","like","in","notIn","isNull","isNotNull"],
              "number":    ["equals","notEquals","greaterThan","lessThan","greaterOrEqual","lessOrEqual","between","in","isNull","isNotNull"],
              "date":      ["equals","notEquals","greaterThan","lessThan","greaterOrEqual","lessOrEqual","between","isNull","isNotNull"],
              "timestamp": ["equals","notEquals","greaterThan","lessThan","greaterOrEqual","lessOrEqual","isNull","isNotNull"]
            }
            """;
}
