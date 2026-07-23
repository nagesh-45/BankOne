package com.bankone.transaction.repository;

import com.bankone.transaction.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByAccountAccountIdOrderByCreatedAtDesc(Long accountId);

    Page<Transaction> findByAccountAccountIdOrderByCreatedAtDesc(Long accountId, Pageable pageable);
}