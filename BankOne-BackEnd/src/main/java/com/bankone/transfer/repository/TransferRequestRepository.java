package com.bankone.transfer.repository;

import com.bankone.transfer.entity.TransferRequestEntity;
import com.bankone.transfer.enums.TransferRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface TransferRequestRepository extends JpaRepository<TransferRequestEntity, Long> {

    List<TransferRequestEntity> findByStatusOrderByCreatedAtAsc(TransferRequestStatus status);

    List<TransferRequestEntity> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    List<TransferRequestEntity> findByStatusInOrderByResolvedAtDesc(
            Collection<TransferRequestStatus> statuses);

    List<TransferRequestEntity> findByResolvedByIgnoreCaseAndStatusInOrderByResolvedAtDesc(
            String resolvedBy,
            Collection<TransferRequestStatus> statuses);

    @Query("""
            SELECT t.status, COUNT(t)
            FROM TransferRequestEntity t
            WHERE t.createdAt >= :fromAt AND t.createdAt < :toAt
            GROUP BY t.status
            ORDER BY t.status
            """)
    List<Object[]> countByStatusInRange(
            @Param("fromAt") LocalDateTime fromAt,
            @Param("toAt") LocalDateTime toAt
    );

    @Query("""
            SELECT COALESCE(t.resolvedBy, 'UNRESOLVED'), COUNT(t)
            FROM TransferRequestEntity t
            WHERE t.createdAt >= :fromAt AND t.createdAt < :toAt
              AND t.status <> :pending
            GROUP BY t.resolvedBy
            ORDER BY COUNT(t) DESC
            """)
    List<Object[]> countByResolvedStaffInRange(
            @Param("fromAt") LocalDateTime fromAt,
            @Param("toAt") LocalDateTime toAt,
            @Param("pending") TransferRequestStatus pending
    );
}
