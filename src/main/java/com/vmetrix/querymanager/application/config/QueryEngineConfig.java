package com.vmetrix.querymanager.application.config;

import com.vmetrix.querymanager.application.service.MetadataService;
import com.vmetrix.querymanager.domain.engine.builder.FromClauseBuilder;
import com.vmetrix.querymanager.domain.engine.builder.OrderByClauseBuilder;
import com.vmetrix.querymanager.domain.engine.builder.QueryAssembler;
import com.vmetrix.querymanager.domain.engine.builder.SelectClauseBuilder;
import com.vmetrix.querymanager.domain.engine.filter.ComparatorStrategyFactory;
import com.vmetrix.querymanager.domain.engine.filter.FilterTreeRenderer;
import com.vmetrix.querymanager.domain.engine.join.JoinResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QueryEngineConfig {

    @Bean
    public SelectClauseBuilder selectClauseBuilder() {
        return new SelectClauseBuilder();
    }

    @Bean
    public FromClauseBuilder fromClauseBuilder(MetadataService metadataService) {
        return new FromClauseBuilder(metadataService);
    }

    @Bean
    public OrderByClauseBuilder orderByClauseBuilder() {
        return new OrderByClauseBuilder();
    }

    @Bean
    public JoinResolver joinResolver() {
        return new JoinResolver();
    }

    @Bean
    public ComparatorStrategyFactory comparatorStrategyFactory() {
        return new ComparatorStrategyFactory();
    }

    @Bean
    public FilterTreeRenderer filterTreeRenderer(ComparatorStrategyFactory comparatorStrategyFactory) {
        return new FilterTreeRenderer(comparatorStrategyFactory);
    }

    @Bean
    public QueryAssembler queryAssembler(MetadataService metadataService,
                                         SelectClauseBuilder selectClauseBuilder,
                                         FromClauseBuilder fromClauseBuilder,
                                         OrderByClauseBuilder orderByClauseBuilder,
                                         JoinResolver joinResolver,
                                         FilterTreeRenderer filterTreeRenderer) {
        return new QueryAssembler(metadataService, selectClauseBuilder, fromClauseBuilder, 
                                  orderByClauseBuilder, joinResolver, filterTreeRenderer);
    }
}
