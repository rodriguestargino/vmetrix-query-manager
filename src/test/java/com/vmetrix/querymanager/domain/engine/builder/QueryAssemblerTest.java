package com.vmetrix.querymanager.domain.engine.builder;

import com.vmetrix.querymanager.domain.engine.filter.FilterResult;
import com.vmetrix.querymanager.domain.engine.filter.FilterTreeRenderer;
import com.vmetrix.querymanager.domain.engine.join.JoinNode;
import com.vmetrix.querymanager.domain.engine.join.JoinResolver;
import com.vmetrix.querymanager.domain.model.EntityMetadata;
import com.vmetrix.querymanager.domain.model.FilterCondition;
import com.vmetrix.querymanager.domain.model.QueryResult;
import com.vmetrix.querymanager.domain.model.SelectField;
import com.vmetrix.querymanager.domain.model.SortDirection;
import com.vmetrix.querymanager.domain.model.SortField;
import com.vmetrix.querymanager.domain.port.MetadataCatalog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QueryAssemblerTest {

    @Mock
    private MetadataCatalog metadataService;
    @Mock
    private SelectClauseBuilder selectBuilder;
    @Mock
    private FromClauseBuilder fromBuilder;
    @Mock
    private OrderByClauseBuilder orderByBuilder;
    @Mock
    private JoinResolver joinResolver;
    @Mock
    private FilterTreeRenderer filterTreeRenderer;

    private QueryAssembler queryAssembler;

    @BeforeEach
    void setUp() {
        queryAssembler = new QueryAssembler(
                metadataService,
                selectBuilder,
                fromBuilder,
                orderByBuilder,
                joinResolver,
                filterTreeRenderer
        );
    }

    @Test
    void should_assemble_complete_query_with_all_clauses() {
        // Arrange
        QuerySpecification spec = QuerySpecification.builder()
                .baseEntity("transaction")
                .selectFields(Arrays.asList(SelectField.builder().entity("transaction").field("txnDate").build()))
                .allRequestedAliases(new HashSet<>(Arrays.asList("transaction", "instrument")))
                .filterBaseNode(FilterCondition.builder().entity("transaction").field("status").comparator("equals").value("SETTLED").build())
                .sortFields(Arrays.asList(SortField.builder().entity("transaction").field("txnDate").direction(SortDirection.DESC).build()))
                .maxResults(10)
                .build();

        // Mocks for builders
        when(selectBuilder.build(eq(spec.getSelectFields()), any())).thenReturn("SELECT t.TXN_DATE");
        when(fromBuilder.build("transaction")).thenReturn("FROM TRANSACTION t");
        when(orderByBuilder.build(eq(spec.getSortFields()), any())).thenReturn("ORDER BY t.TXN_DATE DESC");

        when(metadataService.getAllEntities()).thenReturn(Collections.emptyList());
        when(metadataService.findEntityByLogicalName("transaction"))
                .thenReturn(EntityMetadata.builder().physicalName("TRANSACTION").defaultAlias("t").build());

        // Mocks for Join
        List<JoinNode> joins = Arrays.asList(
                JoinNode.builder()
                        .joinType("LEFT JOIN")
                        .targetTable("INSTRUMENT")
                        .sqlAlias("i")
                        .onClause("t.INSTRUMENT_ID = i.INSTRUMENT_ID")
                        .relationAlias("instrument")
                        .build()
        );
        // Since we need to mock allRelationships and entityMap, we just use any() for simplicity in this orchestration test.
        when(joinResolver.resolve(eq(spec.getAllRequestedAliases()), eq("transaction"), any(), any())).thenReturn(joins);

        // Mocks for Filter
        FilterResult filterResult = FilterResult.builder()
                .sqlFragment("(t.STATUS = :p1)")
                .parameters(Collections.singletonMap("p1", "SETTLED"))
                .build();
        when(filterTreeRenderer.render(eq(spec.getFilterBaseNode()), any(), any(AtomicInteger.class)))
                .thenReturn(filterResult);

        // Act
        QueryResult result = queryAssembler.assemble(spec);

        // Assert
        assertThat(result.getSql()).isEqualTo(
                "SELECT t.TXN_DATE " +
                "FROM TRANSACTION t " +
                "LEFT JOIN INSTRUMENT i ON t.INSTRUMENT_ID = i.INSTRUMENT_ID " +
                "WHERE (t.STATUS = :p1) " +
                "ORDER BY t.TXN_DATE DESC " +
                "FETCH FIRST 10 ROWS ONLY"
        );
        assertThat(result.getParameters()).containsEntry("p1", "SETTLED");
        assertThat(result.getResolvedJoins()).containsExactly("LEFT JOIN INSTRUMENT i ON t.INSTRUMENT_ID = i.INSTRUMENT_ID");
        assertThat(result.getColumnCount()).isEqualTo(1);
    }
    
    @Test
    void should_assemble_query_without_filters() {
        // Arrange
        QuerySpecification spec = QuerySpecification.builder()
                .baseEntity("transaction")
                .selectFields(Arrays.asList(SelectField.builder().entity("transaction").field("txnDate").build()))
                .allRequestedAliases(Collections.singleton("transaction"))
                .sortFields(Collections.emptyList())
                .build();

        when(selectBuilder.build(eq(spec.getSelectFields()), any())).thenReturn("SELECT t.TXN_DATE");
        when(fromBuilder.build("transaction")).thenReturn("FROM TRANSACTION t");
        when(orderByBuilder.build(eq(spec.getSortFields()), any())).thenReturn(""); // empty map
        when(joinResolver.resolve(eq(spec.getAllRequestedAliases()), eq("transaction"), any(), any())).thenReturn(Collections.emptyList());
        when(metadataService.getAllEntities()).thenReturn(Collections.emptyList());
        when(metadataService.findEntityByLogicalName("transaction"))
                .thenReturn(EntityMetadata.builder().physicalName("TRANSACTION").defaultAlias("t").build());

        // Act
        QueryResult result = queryAssembler.assemble(spec);

        // Assert
        assertThat(result.getSql()).isEqualTo("SELECT t.TXN_DATE FROM TRANSACTION t");
        assertThat(result.getParameters()).isEmpty();
    }
}
