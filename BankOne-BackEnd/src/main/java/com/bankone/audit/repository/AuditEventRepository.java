package com.bankone.audit.repository;

import com.bankone.audit.domain.AuditCategory;
import com.bankone.audit.entity.AuditEventEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditEventRepository extends JpaRepository<AuditEventEntity, Long> {

    boolean existsByActionAndTargetTypeAndTargetId(String action, String targetType, String targetId);

    @Query("""
            SELECT e FROM AuditEventEntity e
            WHERE (:category IS NULL OR e.category = :category)
              AND (:action IS NULL OR e.action = :action)
              AND (
                    :actor IS NULL OR :actor = ''
                    OR LOWER(e.actorUsername) LIKE LOWER(CONCAT('%', :actor, '%'))
                  )
            """)
    Page<AuditEventEntity> search(
            @Param("category") AuditCategory category,
            @Param("action") String action,
            @Param("actor") String actor,
            Pageable pageable
    );
}
