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
        // 2. FROM clause
        String fromClause = fromBuilder.build(spec.getBaseEntity());

        // 3. JOIN clauses
        List<RelationshipMetadata> allRelationships = new ArrayList<>();
        Map<String, EntityMetadata> entityMap = new HashMap<>();
        
        if (spec.getAllRequestedAliases() != null) {
            for (String entityName : spec.getAllRequestedAliases()) {
                try { } catch(Exception ignored){}
            }
        }
        metadataService.getAllEntities().forEach(e -> {
            entityMap.put(e.getEntityMetadata().getLogicalName(), e.getEntityMetadata());
            allRelationships.addAll(e.getRelationships());
        });

        List<com.vmetrix.querymanager.domain.engine.join.JoinNode> joinNodes = joinResolver.resolve(spec.getAllRequestedAliases(), spec.getBaseEntity(), allRelationships, entityMap);
        List<String> resolvedJoins = joinNodes.stream()
                .map(j -> j.getJoinType() + " " + j.getTargetTable() + " " + j.getSqlAlias() + " ON " + j.getOnClause())
                .collect(Collectors.toList());
        List<String> resolvedTables = new ArrayList<>();
        resolvedTables.add(metadataService.findEntityByLogicalName(spec.getBaseEntity()).getPhysicalName());
        joinNodes.forEach(j -> resolvedTables.add(j.getTargetTable())); // This fulfills the spec of "in join-dependency order"

        // Common column resolver matching entity logical/alias name to physical SQL columns
        BiFunction<String, String, String> columnResolver = (entity, field) -> {
            // "entity" is the relation alias or logical base entity
            EntityMetadata em;
            String alias;

            // Is it the base entity?
            if (entity.equals(spec.getBaseEntity())) {
                em = metadataService.findEntityByLogicalName(entity);
                alias = em.getDefaultAlias();
            } else {
                // Must be a JOINed entity. Find it in joinNodes.
                com.vmetrix.querymanager.domain.engine.join.JoinNode matchedJoin = joinNodes.stream()
                        .filter(j -> j.getRelationAlias().equals(entity))
                        .findFirst()
                        .orElseThrow(() -> new com.vmetrix.querymanager.shared.exception.UnknownEntityException(entity));
                em = metadataService.findEntityByLogicalName(matchedJoin.getTargetEntity());
                alias = matchedJoin.getSqlAlias();
            }
            
            FieldMetadata fm = metadataService.findField(em.getLogicalName(), field);
            return alias + "." + fm.getPhysicalName();
        };

        // 1. SELECT clause
        String selectClause = selectBuilder.build(spec.getSelectFields(), columnResolver);
        int columnCount = spec.getSelectFields() != null ? spec.getSelectFields().size() : 0;

        // 4. WHERE clause (Filter Tree)
        String whereClause = "";
        Map<String, Object> parameters = new HashMap<>();
        AtomicInteger paramCounter = new AtomicInteger(1);
        int filterCount = 0;

        if (spec.getFilterBaseNode() != null) {
            com.vmetrix.querymanager.domain.engine.filter.FilterResult filterResult = filterTreeRenderer.render(spec.getFilterBaseNode(), columnResolver, paramCounter);
            whereClause = "WHERE " + filterResult.getSqlFragment();
            parameters.putAll(filterResult.getParameters());
            filterCount = parameters.size();
        }

        // 5. ORDER BY clause
        String orderByClause = orderByBuilder.build(spec.getSortFields(), columnResolver);

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
