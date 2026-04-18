package com.vmetrix.querymanager.infrastructure.persistence.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "META_COLUMN")
@Getter
@Setter
public class JpaMetaColumn {

    @Id
    @Column(name = "COLUMN_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TABLE_ID", nullable = false)
    private JpaMetaTable metaTable;

    @Column(name = "LOGICAL_NAME", nullable = false)
    private String logicalName;

    @Column(name = "PHYSICAL_NAME", nullable = false)
    private String physicalName;

    @Column(name = "DATA_TYPE", nullable = false)
    private String dataType;

    @Column(name = "IS_PK", nullable = false)
    private Integer isPk;

    @Column(name = "IS_FK", nullable = false)
    private Integer isFk;

    @Column(name = "FK_TABLE")
    private String fkTable;

    @Column(name = "FK_COLUMN")
    private String fkColumn;

    @Column(name = "IS_FILTERABLE", nullable = false)
    private Integer isFilterable;

    @Column(name = "IS_SELECTABLE", nullable = false)
    private Integer isSelectable;
}
