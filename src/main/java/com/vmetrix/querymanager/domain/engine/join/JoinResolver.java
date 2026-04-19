package com.vmetrix.querymanager.domain.engine.join;

import com.vmetrix.querymanager.domain.model.EntityMetadata;
import com.vmetrix.querymanager.domain.model.RelationshipMetadata;

import java.util.*;
import java.util.stream.Collectors;

/**
 * BFS-based JOIN path resolver. Given a set of requested entity/alias names,
 * traverses the relationship metadata graph starting from the base entity and
 * returns an ordered list of JoinNodes representing the required SQL JOINs.
 */
public class JoinResolver {

    /**
     * Resolves the required JOINs for a set of requested entities/aliases.
     */
    public List<JoinNode> resolve(Set<String> requestedAliases,
                                  String baseEntity,
                                  List<RelationshipMetadata> allRelationships,
                                  Map<String, EntityMetadata> entityMap) {

        Set<String> targetAliases = new HashSet<>(requestedAliases);
        targetAliases.remove(baseEntity);
        if (targetAliases.isEmpty()) {
            return Collections.emptyList();
        }

        // Build adjacency list: sourceEntity -> relationships
        Map<String, List<RelationshipMetadata>> adjacency = allRelationships.stream()
                .collect(Collectors.groupingBy(RelationshipMetadata::getSourceEntity));

        // BFS Data structures
        List<DiscoveryNode> discoveryOrder = new ArrayList<>();
        Map<String, ParentInfo> aliasToParentInfo = new HashMap<>();
        Set<String> visitedAliases = new HashSet<>();
        Queue<BFSNode> queue = new LinkedList<>();

        // Start BFS from base entity (root has no alias, no parent)
        queue.add(new BFSNode(baseEntity, null));

        while (!queue.isEmpty()) {
            BFSNode current = queue.poll();

            List<RelationshipMetadata> outgoing = adjacency.getOrDefault(current.entityName, Collections.emptyList());
            for (RelationshipMetadata rel : outgoing) {
                String relAlias = rel.getRelationAlias();

                if (visitedAliases.contains(relAlias)) {
                    continue;
                }

                visitedAliases.add(relAlias);
                aliasToParentInfo.put(relAlias, new ParentInfo(current.alias, rel, current.entityName));
                discoveryOrder.add(new DiscoveryNode(relAlias, rel));
                
                // Enqueue target to continue BFS
                queue.add(new BFSNode(rel.getTargetEntity(), relAlias));
            }
        }

        // Backtrack to find all required aliases (requested + bridges)
        Set<String> requiredAliases = new HashSet<>();
        for (String requested : targetAliases) {
            String curr = requested;
            while (curr != null) {
                requiredAliases.add(curr);
                ParentInfo parent = aliasToParentInfo.get(curr);
                curr = (parent != null) ? parent.parentAlias : null;
            }
        }

        // Build final JoinNodes in discovery order
        List<JoinNode> result = new ArrayList<>();
        for (DiscoveryNode discovery : discoveryOrder) {
            if (requiredAliases.contains(discovery.alias)) {
                ParentInfo parent = aliasToParentInfo.get(discovery.alias);
                RelationshipMetadata rel = discovery.rel;

                // Resolve source SQL alias: if parentAlias is null, use base entity's default alias
                String sourceSqlAlias = (parent.parentAlias == null)
                        ? entityMap.get(baseEntity).getDefaultAlias()
                        : parent.parentAlias;

                String onClause = String.format("%s.%s = %s.%s",
                        sourceSqlAlias,
                        rel.getSourceColumn(),
                        discovery.alias,
                        rel.getTargetColumn());

                result.add(JoinNode.builder()
                        .sourceTable(entityMap.get(parent.sourceEntity).getPhysicalName())
                        .targetTable(entityMap.get(rel.getTargetEntity()).getPhysicalName())
                        .targetEntity(rel.getTargetEntity())
                        .sqlAlias(discovery.alias)
                        .onClause(onClause)
                        .joinType(rel.getJoinType())
                        .relationAlias(discovery.alias)
                        .build());
            }
        }

        return result;
    }

    private static class BFSNode {
        final String entityName;
        final String alias;

        BFSNode(String entityName, String alias) {
            this.entityName = entityName;
            this.alias = alias;
        }
    }

    private static class ParentInfo {
        final String parentAlias;
        final RelationshipMetadata rel;
        final String sourceEntity;

        ParentInfo(String parentAlias, RelationshipMetadata rel, String sourceEntity) {
            this.parentAlias = parentAlias;
            this.rel = rel;
            this.sourceEntity = sourceEntity;
        }
    }

    private static class DiscoveryNode {
        final String alias;
        final RelationshipMetadata rel;

        DiscoveryNode(String alias, RelationshipMetadata rel) {
            this.alias = alias;
            this.rel = rel;
        }
    }
}
