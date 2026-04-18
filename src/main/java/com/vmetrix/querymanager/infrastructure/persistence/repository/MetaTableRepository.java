package com.vmetrix.querymanager.infrastructure.persistence.repository;

import com.vmetrix.querymanager.infrastructure.persistence.entity.JpaMetaTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MetaTableRepository extends JpaRepository<JpaMetaTable, Long> {
}
