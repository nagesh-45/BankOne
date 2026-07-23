package com.bankone.transaction.service;

import com.bankone.account.entity.Account;
import com.bankone.transaction.entity.Transaction;
import com.bankone.transaction.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.bankone.transaction.dto.TransactionResponse;
import java.math.BigDecimal;

public interface TransactionService {

    Transaction record(
            Account account,
            TransactionType type,
            BigDecimal amount,
            BigDecimal balanceAfter,
            String narration,
            String createdBy
    );
    Page<TransactionResponse> getByAccountId(Long accountId, Pageable pageable);
}