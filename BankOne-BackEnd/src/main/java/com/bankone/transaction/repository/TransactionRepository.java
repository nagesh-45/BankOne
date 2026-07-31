package com.bankone.transaction.repository;

import com.bankone.transaction.entity.Transaction;
import com.bankone.transaction.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByAccountAccountIdOrderByCreatedAtDesc(Long accountId);

    Page<Transaction> findByAccountAccountIdOrderByCreatedAtDesc(Long accountId, Pageable pageable);

    @Query("SELECT t FROM Transaction t JOIN FETCH t.account")
    List<Transaction> findAllWithAccount();

    @Query(
            value = """
                    SELECT t FROM Transaction t
                    JOIN t.account a
                    LEFT JOIN a.customer c
                    WHERE (:accountId IS NULL OR a.accountId = :accountId)
                      AND (:type IS NULL OR t.transactionType = :type)
                      AND (
                            :search IS NULL OR :search = ''
                            OR LOWER(a.accountNumber) LIKE LOWER(CONCAT('%', :search, '%'))
                            OR LOWER(COALESCE(t.narration, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                            OR LOWER(COALESCE(t.createdBy, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                            OR LOWER(COALESCE(c.firstName, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                            OR LOWER(COALESCE(c.lastName, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                            OR CAST(c.customerId AS string) LIKE CONCAT('%', :search, '%')
                      )
                    """,
            countQuery = """
                    SELECT COUNT(t) FROM Transaction t
                    JOIN t.account a
                    LEFT JOIN a.customer c
                    WHERE (:accountId IS NULL OR a.accountId = :accountId)
                      AND (:type IS NULL OR t.transactionType = :type)
                      AND (
                            :search IS NULL OR :search = ''
                            OR LOWER(a.accountNumber) LIKE LOWER(CONCAT('%', :search, '%'))
                            OR LOWER(COALESCE(t.narration, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                            OR LOWER(COALESCE(t.createdBy, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                            OR LOWER(COALESCE(c.firstName, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                            OR LOWER(COALESCE(c.lastName, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                            OR CAST(c.customerId AS string) LIKE CONCAT('%', :search, '%')
                      )
                    """
    )
    Page<Transaction> searchStaffTransactions(
            @Param("accountId") Long accountId,
            @Param("type") TransactionType type,
            @Param("search") String search,
            Pageable pageable
    );

    @Query(value = """
            SELECT CAST(created_at AS date) AS day,
                   transaction_type,
                   COALESCE(SUM(amount), 0) AS total_amount,
                   COUNT(*) AS txn_count
            FROM bank_transaction
            WHERE created_at >= :fromTs
              AND created_at < :toTs
            GROUP BY CAST(created_at AS date), transaction_type
            ORDER BY day
            """, nativeQuery = true)
    List<Object[]> aggregateDailyByType(
            @Param("fromTs") java.time.Instant fromTs,
            @Param("toTs") java.time.Instant toTs
    );
}
