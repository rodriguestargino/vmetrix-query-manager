package com.vmetrix.querymanager.infrastructure.persistence.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "META_RELATIONSHIP")
@Getter
@Setter
public class JpaMetaRelationship {

    @Id
    @Column(name = "REL_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SOURCE_TABLE_ID", nullable = false)
    private JpaMetaTable sourceTable;

    @Column(name = "SOURCE_COLUMN", nullable = false)
    private String sourceColumn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TARGET_TABLE_ID", nullable = false)
    private JpaMetaTable targetTable;

    @Column(name = "TARGET_COLUMN", nullable = false)
    private String targetColumn;

    @Column(name = "JOIN_TYPE", nullable = false)
    private String joinType;

    @Column(name = "RELATION_ALIAS", nullable = false)
    private String relationAlias;
}
