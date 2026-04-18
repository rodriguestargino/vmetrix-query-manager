package com.vmetrix.querymanager.domain.engine.join;

import com.vmetrix.querymanager.domain.model.EntityMetadata;
import com.vmetrix.querymanager.domain.model.RelationshipMetadata;

import java.util.*;
import java.util.stream.Collectors;

/**
 * BFS-based JOIN path resolver. Given a set of requested entity/alias names,
 * traverses the relationship metadata graph starting from the base entity and
 * returns an ordered list of JoinNodes representing the required SQL JOINs.
 *
 * <p>This is a pure domain class — no Spring annotations, no JPA imports.
 * All metadata is passed as method parameters.</p>
 *
 * <h3>Key design rules:</h3>
 * <ul>
 *   <li>Uses RELATION_ALIAS as the SQL table alias (not DEFAULT_ALIAS) to prevent
 *       collisions when the same table (e.g. PARTY) is joined via different paths.</li>
 *   <li>Automatically includes bridge tables needed to reach chained JOINs
 *       (e.g. INSTRUMENT is included when resolving issuer even if not requested).</li>
 *   <li>JOIN order respects dependency — direct JOINs appear before chained JOINs.</li>
 *   <li>No hardcoded table or column names — all from metadata.</li>
 * </ul>
 */
public class JoinResolver {

    /**
     * Resolves the required JOINs for a set of requested entities/aliases.
     *
     * @param requestedAliases set of logical entity names or relation aliases requested
     *                         (e.g. {"transaction", "instrument", "counterparty", "issuer"})
     * @param baseEntity       the root entity for the query (e.g. "transaction")
     * @param allRelationships all relationship metadata from the database
     * @param entityMap        mapping of logical entity name → EntityMetadata
     * @return ordered list of JoinNodes representing required SQL JOINs
     */
    public List<JoinNode> resolve(Set<String> requestedAliases,
                                  String baseEntity,
                                  List<RelationshipMetadata> allRelationships,
                                  Map<String, EntityMetadata> entityMap) {

        // If only the base entity is requested, no JOINs needed
        Set<String> nonBaseAliases = new LinkedHashSet<>(requestedAliases);
        nonBaseAliases.remove(baseEntity);
        if (nonBaseAliases.isEmpty()) {
            return Collections.emptyList();
        }

        // Build adjacency list: sourceEntity → list of relationships
        Map<String, List<RelationshipMetadata>> adjacency = allRelationships.stream()
                .collect(Collectors.groupingBy(RelationshipMetadata::getSourceEntity));

        // BFS from base entity
        List<JoinNode> resolvedJoins = new ArrayList<>();
        Set<String> visitedAliases = new HashSet<>();   // track relation aliases already joined
        Set<String> satisfiedAliases = new HashSet<>();  // track which requested aliases are satisfied
        Queue<String> queue = new LinkedList<>();
        Set<String> enqueued = new HashSet<>();

        queue.add(baseEntity);
        enqueued.add(baseEntity);

        while (!queue.isEmpty() && satisfiedAliases.size() < nonBaseAliases.size()) {
            String currentEntity = queue.poll();

            List<RelationshipMetadata> outgoing = adjacency.getOrDefault(currentEntity, Collections.emptyList());

            for (RelationshipMetadata rel : outgoing) {
                String relAlias = rel.getRelationAlias();

                // Skip already-visited relation aliases (prevents duplicates)
                if (visitedAliases.contains(relAlias)) {
                    continue;
                }

                // Determine if this relationship is needed:
                // 1. Its relation alias is directly requested, OR
                // 2. Following it may lead to a requested alias (BFS discovery)
                boolean directlyRequested = nonBaseAliases.contains(relAlias);
                boolean potentialBridge = isNeededAsBridge(rel.getTargetEntity(), nonBaseAliases,
                        satisfiedAliases, adjacency, visitedAliases);

                if (!directlyRequested && !potentialBridge) {
                    continue;
                }

                // Build the JoinNode
                EntityMetadata sourceEntityMeta = entityMap.get(currentEntity);
                EntityMetadata targetEntityMeta = entityMap.get(rel.getTargetEntity());

                // Determine SQL alias for source: use the relation alias if the source was joined
                // via a relationship, otherwise use the default alias
                String sourceAlias = resolveSourceAlias(currentEntity, baseEntity, resolvedJoins, entityMap);
                String targetAlias = relAlias;

                String onClause = String.format("%s.%s = %s.%s",
                        sourceAlias,
                        rel.getSourceColumn(),
                        targetAlias,
                        rel.getTargetColumn());

                JoinNode joinNode = JoinNode.builder()
                        .sourceTable(sourceEntityMeta.getPhysicalName())
                        .targetTable(targetEntityMeta.getPhysicalName())
                        .sqlAlias(targetAlias)
                        .onClause(onClause)
                        .joinType(rel.getJoinType())
                        .relationAlias(relAlias)
                        .build();

                resolvedJoins.add(joinNode);
                visitedAliases.add(relAlias);

                if (directlyRequested) {
                    satisfiedAliases.add(relAlias);
                }

                // Enqueue the target entity for further BFS exploration
                // Use a unique key combining entity + relation alias to allow
                // the same physical table to be reached via different paths
                if (!enqueued.contains(rel.getTargetEntity() + ":" + relAlias)) {
                    queue.add(rel.getTargetEntity());
                    enqueued.add(rel.getTargetEntity() + ":" + relAlias);
                }
            }
        }

        return resolvedJoins;
    }

    /**
     * Checks whether a target entity is needed as a bridge to reach
     * any still-unsatisfied requested alias through transitive relationships.
     */
    private boolean isNeededAsBridge(String targetEntity,
                                     Set<String> nonBaseAliases,
                                     Set<String> satisfiedAliases,
                                     Map<String, List<RelationshipMetadata>> adjacency,
                                     Set<String> visitedAliases) {
        // Check if any relationship from the target entity leads to an unsatisfied requested alias
        List<RelationshipMetadata> outgoing = adjacency.getOrDefault(targetEntity, Collections.emptyList());
        for (RelationshipMetadata rel : outgoing) {
            if (!visitedAliases.contains(rel.getRelationAlias())
                    && nonBaseAliases.contains(rel.getRelationAlias())
                    && !satisfiedAliases.contains(rel.getRelationAlias())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Resolves the SQL alias for the source entity in a JOIN ON clause.
     * If the source is the base entity, uses its default alias.
     * If the source was joined via a relationship, uses the relation alias from that JOIN.
     */
    private String resolveSourceAlias(String currentEntity,
                                      String baseEntity,
                                      List<JoinNode> resolvedJoins,
                                      Map<String, EntityMetadata> entityMap) {
        if (currentEntity.equals(baseEntity)) {
            return entityMap.get(baseEntity).getDefaultAlias();
        }

        // Find the JoinNode that brought this entity in — use its sql alias
        // We look for a join where the target entity matches and use the relation alias
        for (JoinNode join : resolvedJoins) {
            if (join.getRelationAlias().equals(currentEntity) ||
                    (entityMap.get(currentEntity) != null &&
                     join.getTargetTable().equals(entityMap.get(currentEntity).getPhysicalName()) &&
                     join.getRelationAlias().equals(currentEntity))) {
                return join.getSqlAlias();
            }
        }

        // Fallback: if the entity was added as a bridge, find the join that targets it
        for (JoinNode join : resolvedJoins) {
            EntityMetadata meta = entityMap.get(currentEntity);
            if (meta != null && join.getTargetTable().equals(meta.getPhysicalName())) {
                return join.getSqlAlias();
            }
        }

        // Final fallback: use default alias
        return entityMap.get(currentEntity).getDefaultAlias();
    }
}
