package com.vmetrix.querymanager.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SortField {
    private String entity;
    private String field;
    private SortDirection direction;
}
