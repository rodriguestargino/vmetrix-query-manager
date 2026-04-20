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
 */
class JoinResolverTest {

    private JoinResolver joinResolver;
    private Map<String, EntityMetadata> entityMap;
    private List<RelationshipMetadata> allRelationships;

    @BeforeEach
    void setUp() {
        joinResolver = new JoinResolver();

        entityMap = new HashMap<>();
        entityMap.put("transaction", EntityMetadata.builder()
                .logicalName("transaction").physicalName("TRANSACTION").defaultAlias("t").build());
        entityMap.put("instrument", EntityMetadata.builder()
                .logicalName("instrument").physicalName("INSTRUMENT").defaultAlias("i").build());
        entityMap.put("party", EntityMetadata.builder()
                .logicalName("party").physicalName("PARTY").defaultAlias("p").build());

        allRelationships = Arrays.asList(
                RelationshipMetadata.builder()
                        .sourceEntity("transaction").sourceColumn("INSTRUMENT_ID")
                        .targetEntity("instrument").targetColumn("INSTRUMENT_ID")
                        .joinType("LEFT JOIN").relationAlias("instrument").build(),
                RelationshipMetadata.builder()
                        .sourceEntity("transaction").sourceColumn("COUNTERPARTY_ID")
                        .targetEntity("party").targetColumn("PARTY_ID")
                        .joinType("LEFT JOIN").relationAlias("counterparty").build(),
                RelationshipMetadata.builder()
                        .sourceEntity("instrument").sourceColumn("ISSUER_ID")
                        .targetEntity("party").targetColumn("PARTY_ID")
                        .joinType("LEFT JOIN").relationAlias("issuer").build()
        );
    }

    @Test
    @DisplayName("should return empty joins when single entity requested")
    void should_return_empty_joins_when_single_entity_requested() {
        Set<String> requested = Collections.singleton("transaction");
        List<JoinNode> result = joinResolver.resolve(requested, "transaction", allRelationships, entityMap);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("should return one join when direct relationship exists")
    void should_return_one_join_when_direct_relationship_exists() {
        Set<String> requested = new LinkedHashSet<>(Arrays.asList("transaction", "instrument"));
        List<JoinNode> result = joinResolver.resolve(requested, "transaction", allRelationships, entityMap);
        assertEquals(1, result.size());
        assertEquals("instrument", result.get(0).getRelationAlias());
        assertEquals("t.INSTRUMENT_ID = instrument.INSTRUMENT_ID", result.get(0).getOnClause());
    }

    @Test
    @DisplayName("should return aliased join when counterparty requested")
    void should_return_aliased_join_when_counterparty_requested() {
        Set<String> requested = new LinkedHashSet<>(Arrays.asList("transaction", "counterparty"));
        List<JoinNode> result = joinResolver.resolve(requested, "transaction", allRelationships, entityMap);
        assertEquals(1, result.size());
        assertEquals("counterparty", result.get(0).getSqlAlias());
    }

    @Test
    @DisplayName("should return chained joins when issuer requested")
    void should_return_chained_joins_when_issuer_requested() {
        Set<String> requested = new LinkedHashSet<>(Arrays.asList("transaction", "issuer"));
        List<JoinNode> result = joinResolver.resolve(requested, "transaction", allRelationships, entityMap);
        
        assertEquals(2, result.size(), "Expected instrument bridge + issuer");
        assertEquals("instrument", result.get(0).getRelationAlias());
        assertEquals("issuer", result.get(1).getRelationAlias());
        assertEquals("instrument.ISSUER_ID = issuer.PARTY_ID", result.get(1).getOnClause());
    }

    @Test
    @DisplayName("should resolve multi-hop joins correctly (3 hops: A->B->C->D)")
    void should_resolve_multi_hop_joins_correctly() {
        entityMap.put("bridgeA", EntityMetadata.builder().logicalName("bridgeA").physicalName("TABLE_A").defaultAlias("a").build());
        entityMap.put("bridgeB", EntityMetadata.builder().logicalName("bridgeB").physicalName("TABLE_B").defaultAlias("b").build());
        entityMap.put("targetC", EntityMetadata.builder().logicalName("targetC").physicalName("TABLE_C").defaultAlias("c").build());

        List<RelationshipMetadata> chain = new ArrayList<>();
        chain.add(RelationshipMetadata.builder()
                .sourceEntity("transaction").targetEntity("bridgeA").relationAlias("relA")
                .sourceColumn("A_ID").targetColumn("ID").joinType("INNER JOIN").build());
        chain.add(RelationshipMetadata.builder()
                .sourceEntity("bridgeA").targetEntity("bridgeB").relationAlias("relB")
                .sourceColumn("B_ID").targetColumn("ID").joinType("INNER JOIN").build());
        chain.add(RelationshipMetadata.builder()
                .sourceEntity("bridgeB").targetEntity("targetC").relationAlias("relC")
                .sourceColumn("C_ID").targetColumn("ID").joinType("INNER JOIN").build());

        Set<String> requested = new HashSet<>(Arrays.asList("transaction", "relC"));
        List<JoinNode> result = joinResolver.resolve(requested, "transaction", chain, entityMap);

        assertEquals(3, result.size(), "Should have 3 joins for the full chain");
        assertEquals("relA", result.get(0).getRelationAlias());
        assertEquals("relB", result.get(1).getRelationAlias());
        assertEquals("relC", result.get(2).getRelationAlias());
        
        assertEquals("t.A_ID = relA.ID", result.get(0).getOnClause());
        assertEquals("relA.B_ID = relB.ID", result.get(1).getOnClause());
        assertEquals("relB.C_ID = relC.ID", result.get(2).getOnClause());
    }

    @Test
    @DisplayName("should not duplicate join when entity appears twice in select")
    void should_not_duplicate_join_when_entity_appears_twice_in_select() {
        Set<String> requested = new LinkedHashSet<>(Arrays.asList("transaction", "instrument"));
        List<JoinNode> result = joinResolver.resolve(requested, "transaction", allRelationships, entityMap);
        assertEquals(1, result.size());
        assertEquals("instrument", result.get(0).getRelationAlias());
    }
}
