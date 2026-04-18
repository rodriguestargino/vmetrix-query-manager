package com.vmetrix.querymanager.domain.engine.builder;

import com.vmetrix.querymanager.domain.model.FilterNode;
import com.vmetrix.querymanager.domain.model.SelectField;
import com.vmetrix.querymanager.domain.model.SortField;
import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.Set;

@Value
@Builder
public class QuerySpecification {
    String baseEntity;
    List<SelectField> selectFields;
    Set<String> allRequestedAliases;
    FilterNode filterBaseNode;
    List<SortField> sortFields;
    Integer maxResults;
}
