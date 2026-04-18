package com.vmetrix.querymanager.infrastructure.persistence.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "META_TABLE")
@Getter
@Setter
public class JpaMetaTable {

    @Id
    @Column(name = "TABLE_ID")
    private Long id;

    @Column(name = "LOGICAL_NAME", nullable = false, unique = true)
    private String logicalName;

    @Column(name = "PHYSICAL_NAME", nullable = false)
    private String physicalName;

    @Column(name = "DEFAULT_ALIAS", nullable = false)
    private String defaultAlias;

    @Column(name = "DESCRIPTION")
    private String description;
}
