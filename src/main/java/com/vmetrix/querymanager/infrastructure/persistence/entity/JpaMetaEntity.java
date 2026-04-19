package com.vmetrix.querymanager.infrastructure.persistence.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "META_ENTITY")
@Getter
@Setter
public class JpaMetaEntity {

    @Id
    @Column(name = "ENTITY_ID")
    private Long id;

    @Column(name = "ENTITY_NAME", nullable = false, unique = true)
    private String entityName;

    @Column(name = "PHYSICAL_TABLE", nullable = false)
    private String physicalTable;

    @Column(name = "DEFAULT_ALIAS", nullable = false)
    private String defaultAlias;

    @Column(name = "DESCRIPTION")
    private String description;
}
