package com.vmetrix.querymanager.infrastructure.persistence.repository;

import com.vmetrix.querymanager.infrastructure.persistence.entity.JpaMetaColumn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MetaColumnRepository extends JpaRepository<JpaMetaColumn, Long> {
    List<JpaMetaColumn> findAllByMetaEntityId(Long entityId);
}
