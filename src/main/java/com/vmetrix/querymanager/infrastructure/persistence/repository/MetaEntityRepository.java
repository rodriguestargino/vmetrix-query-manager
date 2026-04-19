package com.vmetrix.querymanager.infrastructure.persistence.repository;

import com.vmetrix.querymanager.infrastructure.persistence.entity.JpaMetaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MetaEntityRepository extends JpaRepository<JpaMetaEntity, Long> {
}
