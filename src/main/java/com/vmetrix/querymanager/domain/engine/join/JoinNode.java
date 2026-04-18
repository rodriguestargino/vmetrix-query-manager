package com.vmetrix.querymanager.domain.engine.join;

import lombok.Builder;
import lombok.Value;

/**
 * Immutable domain model representing a single resolved SQL JOIN.
 * Built by JoinResolver during BFS graph traversal over relationship metadata.
 */
@Value
@Builder
public class JoinNode {

    /** Physical table name of the source entity (e.g. "TRANSACTION") */
    String sourceTable;

    /** Physical table name of the target entity (e.g. "INSTRUMENT") */
    String targetTable;

    /** SQL alias used in the query, derived from RELATION_ALIAS (e.g. "counterparty") */
    String sqlAlias;

    /** Full ON clause string (e.g. "t.INSTRUMENT_ID = i.INSTRUMENT_ID") */
    String onClause;

    /** JOIN type from metadata (e.g. "LEFT JOIN") */
    String joinType;

    /** Logical relation alias from metadata (e.g. "instrument", "counterparty", "issuer") */
    String relationAlias;
}
