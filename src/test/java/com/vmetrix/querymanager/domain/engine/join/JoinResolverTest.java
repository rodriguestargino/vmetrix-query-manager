package com.vmetrix.querymanager.domain.engine.join;

import com.vmetrix.querymanager.domain.model.EntityMetadata;
import com.vmetrix.querymanager.domain.model.RelationshipMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TDD unit tests for JoinResolver — BFS graph traversal over relationship metadata.
 * Pure domain tests: no Spring context, no Mockito.
 */
class JoinResolverTest {

    private JoinResolver joinResolver;

    // ── Entity metadata fixtures ──
    private Map<String, EntityMetadata> entityMap;

    // ── Relationship metadata fixtures ──
    // Mirrors data.sql:
    //   1: transaction → instrument via INSTRUMENT_ID (alias: instrument)
    //   2: transaction → party via COUNTERPARTY_ID (alias: counterparty)
    //   3: instrument → party via ISSUER_ID (alias: issuer)
    private List<RelationshipMetadata> allRelationships;

    @BeforeEach
    void setUp() {
        joinResolver = new JoinResolver();

        entityMap = new HashMap<>();
        entityMap.put("transaction", EntityMetadata.builder()
                .logicalName("transaction")
                .physicalName("TRANSACTION")
                .defaultAlias("t")
                .description("Financial transactions")
                .build());
        entityMap.put("instrument", EntityMetadata.builder()
                .logicalName("instrument")
                .physicalName("INSTRUMENT")
                .defaultAlias("i")
                .description("Financial instruments")
                .build());
        entityMap.put("party", EntityMetadata.builder()
                .logicalName("party")
                .physicalName("PARTY")
                .defaultAlias("p")
                .description("Counterparties and issuers")
                .build());

        allRelationships = List.of(
                RelationshipMetadata.builder()
                        .sourceEntity("transaction")
                        .sourceColumn("INSTRUMENT_ID")
                        .targetEntity("instrument")
                        .targetColumn("INSTRUMENT_ID")
                        .joinType("LEFT JOIN")
                        .relationAlias("instrument")
                        .build(),
                RelationshipMetadata.builder()
                        .sourceEntity("transaction")
                        .sourceColumn("COUNTERPARTY_ID")
                        .targetEntity("party")
                        .targetColumn("PARTY_ID")
                        .joinType("LEFT JOIN")
                        .relationAlias("counterparty")
                        .build(),
                RelationshipMetadata.builder()
                        .sourceEntity("instrument")
                        .sourceColumn("ISSUER_ID")
                        .targetEntity("party")
                        .targetColumn("PARTY_ID")
                        .joinType("LEFT JOIN")
                        .relationAlias("issuer")
                        .build()
        );
    }

    // ── Test 1: Single entity, no JOIN needed ──

    @Test
    @DisplayName("should return empty joins when single entity requested")
    void should_return_empty_joins_when_single_entity_requested() {
        Set<String> requested = Set.of("transaction");

        List<JoinNode> result = joinResolver.resolve(requested, "transaction", allRelationships, entityMap);

        assertTrue(result.isEmpty(), "No JOINs needed when only base entity is requested");
    }

    // ── Test 2: Direct JOIN (transaction → instrument) ──

    @Test
    @DisplayName("should return one join when direct relationship exists")
    void should_return_one_join_when_direct_relationship_exists() {
        Set<String> requested = new LinkedHashSet<>(List.of("transaction", "instrument"));

        List<JoinNode> result = joinResolver.resolve(requested, "transaction", allRelationships, entityMap);

        assertEquals(1, result.size(), "Expected exactly 1 JOIN");

        JoinNode join = result.get(0);
        assertEquals("TRANSACTION", join.getSourceTable());
        assertEquals("INSTRUMENT", join.getTargetTable());
        assertEquals("instrument", join.getRelationAlias());
        assertEquals("LEFT JOIN", join.getJoinType());
        assertEquals("t.INSTRUMENT_ID = instrument.INSTRUMENT_ID", join.getOnClause());
    }

    // ── Test 3: Aliased JOIN (transaction → counterparty via PARTY) ──

    @Test
    @DisplayName("should return aliased join when counterparty requested")
    void should_return_aliased_join_when_counterparty_requested() {
        Set<String> requested = new LinkedHashSet<>(List.of("transaction", "counterparty"));

        List<JoinNode> result = joinResolver.resolve(requested, "transaction", allRelationships, entityMap);

        assertEquals(1, result.size(), "Expected exactly 1 JOIN");

        JoinNode join = result.get(0);
        assertEquals("TRANSACTION", join.getSourceTable());
        assertEquals("PARTY", join.getTargetTable());
        assertEquals("counterparty", join.getSqlAlias());
        assertEquals("counterparty", join.getRelationAlias());
        assertEquals("LEFT JOIN", join.getJoinType());
        assertEquals("t.COUNTERPARTY_ID = counterparty.PARTY_ID", join.getOnClause());
    }

    // ── Test 4: Chained JOIN (transaction → instrument → issuer via PARTY) ──

    @Test
    @DisplayName("should return chained joins when issuer requested")
    void should_return_chained_joins_when_issuer_requested() {
        Set<String> requested = new LinkedHashSet<>(List.of("transaction", "issuer"));

        List<JoinNode> result = joinResolver.resolve(requested, "transaction", allRelationships, entityMap);

        assertEquals(2, result.size(), "Expected 2 JOINs (instrument as bridge + issuer)");

        // First JOIN: transaction → instrument (bridge)
        JoinNode bridge = result.get(0);
        assertEquals("TRANSACTION", bridge.getSourceTable());
        assertEquals("INSTRUMENT", bridge.getTargetTable());
        assertEquals("instrument", bridge.getRelationAlias());

        // Second JOIN: instrument → party (issuer)
        JoinNode issuer = result.get(1);
        assertEquals("INSTRUMENT", issuer.getSourceTable());
        assertEquals("PARTY", issuer.getTargetTable());
        assertEquals("issuer", issuer.getSqlAlias());
        assertEquals("issuer", issuer.getRelationAlias());
        assertEquals("instrument.ISSUER_ID = issuer.PARTY_ID", issuer.getOnClause());
    }

    // ── Test 5: All 4 entities → 3 JOINs, correct order ──

    @Test
    @DisplayName("should return three joins when all entities requested")
    void should_return_three_joins_when_all_entities_requested() {
        Set<String> requested = new LinkedHashSet<>(List.of("transaction", "instrument", "counterparty", "issuer"));

        List<JoinNode> result = joinResolver.resolve(requested, "transaction", allRelationships, entityMap);

        assertEquals(3, result.size(), "Expected exactly 3 JOINs");

        // Verify all relation aliases are present
        List<String> aliases = result.stream()
                .map(JoinNode::getRelationAlias)
                .toList();
        assertTrue(aliases.contains("instrument"), "instrument JOIN must be present");
        assertTrue(aliases.contains("counterparty"), "counterparty JOIN must be present");
        assertTrue(aliases.contains("issuer"), "issuer JOIN must be present");

        // Verify dependency order: instrument must come before issuer
        int instrumentIdx = aliases.indexOf("instrument");
        int issuerIdx = aliases.indexOf("issuer");
        assertTrue(instrumentIdx < issuerIdx,
                "instrument JOIN must appear before issuer JOIN (dependency order)");
    }

    // ── Test 6: Duplicate entity in select → JOIN appears only once ──

    @Test
    @DisplayName("should not duplicate join when entity appears twice in select")
    void should_not_duplicate_join_when_entity_appears_twice_in_select() {
        // Even if "instrument" is somehow requested multiple times, only 1 JOIN
        Set<String> requested = new LinkedHashSet<>(List.of("transaction", "instrument"));

        List<JoinNode> result = joinResolver.resolve(requested, "transaction", allRelationships, entityMap);

        assertEquals(1, result.size(), "JOIN should appear only once regardless of duplicate requests");
        assertEquals("instrument", result.get(0).getRelationAlias());
    }
}
