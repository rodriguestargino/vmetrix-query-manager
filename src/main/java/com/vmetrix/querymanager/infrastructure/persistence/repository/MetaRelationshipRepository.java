package com.vmetrix.querymanager.infrastructure.persistence.repository;

import com.vmetrix.querymanager.infrastructure.persistence.entity.JpaMetaRelationship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MetaRelationshipRepository extends JpaRepository<JpaMetaRelationship, Long> {
    List<JpaMetaRelationship> findAllBySourceEntityId(Long sourceEntityId);
}
