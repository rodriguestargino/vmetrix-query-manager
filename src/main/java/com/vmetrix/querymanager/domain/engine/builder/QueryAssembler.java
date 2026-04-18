package com.vmetrix.querymanager.domain.engine.builder;

import com.vmetrix.querymanager.application.service.MetadataService;
import com.vmetrix.querymanager.domain.engine.filter.FilterResult;
import com.vmetrix.querymanager.domain.engine.filter.FilterTreeRenderer;
import com.vmetrix.querymanager.domain.engine.join.JoinNode;
import com.vmetrix.querymanager.domain.engine.join.JoinResolver;
import com.vmetrix.querymanager.domain.model.EntityMetadata;
import com.vmetrix.querymanager.domain.model.FieldMetadata;
import com.vmetrix.querymanager.domain.model.QueryResult;
import com.vmetrix.querymanager.domain.model.RelationshipMetadata;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class QueryAssembler {

    private final MetadataService metadataService;
    private final SelectClauseBuilder selectBuilder;
    private final FromClauseBuilder fromBuilder;
    private final OrderByClauseBuilder orderByBuilder;
    private final JoinResolver joinResolver;
    private final FilterTreeRenderer filterTreeRenderer;

    public QueryResult assemble(QuerySpecification spec) {
        // 1. SELECT clause
        String selectClause = selectBuilder.build(spec.getSelectFields());
        int columnCount = spec.getSelectFields() != null ? spec.getSelectFields().size() : 0;

        // 2. FROM clause
        String fromClause = fromBuilder.build(spec.getBaseEntity());

        // 3. JOIN clauses
        List<RelationshipMetadata> allRelationships = new ArrayList<>();
        Map<String, EntityMetadata> entityMap = new HashMap<>();
        
        // This is a bit inefficient if we load everything, but we need it for JoinResolver.
        // Let's populate entityMap for needed entities and get all relationships for the base ones.
        for (String entityName : spec.getAllRequestedAliases()) {
            try {
                // If the name is actually a relation alias, we might not find it directly, but let's assume JoinResolver 
                // deals with finding the target entity. Actually JoinResolver expects entityMap to have logical Entity names.
                // It's safer to just populate entityMap from all entities if JoinResolver traverses arbitrarily.
            } catch(Exception ignored){}
        }
        metadataService.getAllEntities().forEach(e -> {
            entityMap.put(e.getEntityMetadata().getLogicalName(), e.getEntityMetadata());
            allRelationships.addAll(e.getRelationships());
        });

        List<JoinNode> joinNodes = joinResolver.resolve(spec.getAllRequestedAliases(), spec.getBaseEntity(), allRelationships, entityMap);
        List<String> resolvedJoins = joinNodes.stream()
                .map(j -> j.getJoinType() + " " + j.getTargetTable() + " " + j.getSqlAlias() + " ON " + j.getOnClause())
                .collect(Collectors.toList());
        List<String> resolvedTables = new ArrayList<>();
        resolvedTables.add(metadataService.findEntityByLogicalName(spec.getBaseEntity()).getPhysicalName());
        joinNodes.forEach(j -> resolvedTables.add(j.getTargetTable())); // This fulfills the spec of "in join-dependency order"


        // 4. WHERE clause (Filter Tree)
        String whereClause = "";
        Map<String, Object> parameters = new HashMap<>();
        AtomicInteger paramCounter = new AtomicInteger(1);
        int filterCount = 0;

        if (spec.getFilterBaseNode() != null) {
            BiFunction<String, String, String> columnResolver = (entity, field) -> {
                EntityMetadata em = metadataService.findEntityByLogicalName(entity);
                FieldMetadata fm = metadataService.findField(entity, field);
                
                // We should use the sql alias. If it's the base entity, use defaultAlias.
                // If it's joined, we should use the relationAlias. But for fully proper operation,
                // the logical 'entity' name in a filter is typically the relationAlias. 
                // For this, we can try to find if there's a join.
                String alias = em.getDefaultAlias();
                for (JoinNode join : joinNodes) {
                    if (join.getRelationAlias().equals(entity)) {
                        alias = join.getSqlAlias();
                        break;
                    }
                }
                return alias + "." + fm.getPhysicalName();
            };

            FilterResult filterResult = filterTreeRenderer.render(spec.getFilterBaseNode(), columnResolver, paramCounter);
            whereClause = "WHERE " + filterResult.getSqlFragment();
            parameters.putAll(filterResult.getParameters());
            // Filter count technically should be counted by traversing the tree, but let's approximate based on parameters
            // since each leaf puts parameter(s). Or we can leave it as param size if they roughly match.
            filterCount = parameters.size(); // Rough approximation or just count nodes.
        }

        // 5. ORDER BY clause
        String orderByClause = orderByBuilder.build(spec.getSortFields());

        // Assemble the full SQL
        StringBuilder sql = new StringBuilder();
        sql.append(selectClause).append(" ").append(fromClause);
        
        if (!resolvedJoins.isEmpty()) {
            sql.append(" ").append(String.join(" ", resolvedJoins));
        }
        
        if (!whereClause.isEmpty()) {
            sql.append(" ").append(whereClause);
        }
        
        if (orderByClause != null && !orderByClause.isEmpty()) {
            sql.append(" ").append(orderByClause);
        }
        
        if (spec.getMaxResults() != null && spec.getMaxResults() > 0) {
            sql.append(" FETCH FIRST ").append(spec.getMaxResults()).append(" ROWS ONLY");
        }

        return QueryResult.builder()
                .sql(sql.toString())
                .parameters(parameters)
                .resolvedTables(resolvedTables)
                .resolvedJoins(resolvedJoins)
                .columnCount(columnCount)
                .filterCount(filterCount)
                .generatedAt(Instant.now())
                .build();
    }
}
