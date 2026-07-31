package com.bankone.transaction.service;

import com.bankone.account.entity.Account;
import com.bankone.account.repository.AccountRepository;
import com.bankone.customer.entity.Customer;
import com.bankone.transaction.dto.TransactionResponse;
import com.bankone.transaction.entity.Transaction;
import com.bankone.transaction.enums.TransactionType;
import com.bankone.transaction.repository.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public TransactionServiceImpl(TransactionRepository transactionRepository,
                                  AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    @Transactional
    public Transaction record(
            Account account,
            TransactionType type,
            BigDecimal amount,
            BigDecimal balanceAfter,
            String narration,
            String createdBy
    ) {
        if (account == null) {
            throw new IllegalArgumentException("Account is required");
        }
        if (type == null) {
            throw new IllegalArgumentException("Transaction type is required");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        if (balanceAfter == null) {
            throw new IllegalArgumentException("Balance after is required");
        }

        Transaction tx = new Transaction();
        tx.setAccount(account);
        tx.setTransactionType(type);
        tx.setAmount(amount);
        tx.setBalanceAfter(balanceAfter);
        tx.setCurrencyCode(account.getCurrencyCode());
        tx.setNarration(narration);
        tx.setCreatedBy(createdBy);

        return transactionRepository.save(tx);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getByAccountId(Long accountId, Pageable pageable) {
        if (!accountRepository.existsById(accountId)) {
            throw new IllegalArgumentException("Account not found");
        }
        return transactionRepository
                .findByAccountAccountIdOrderByCreatedAtDesc(accountId, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> listAll(
            Long accountId,
            TransactionType type,
            String search,
            Pageable pageable
    ) {
        String q = StringUtils.hasText(search) ? search.trim() : null;
        return transactionRepository
                .searchStaffTransactions(accountId, type, q, pageable)
                .map(this::toResponse);
    }

    private TransactionResponse toResponse(Transaction tx) {
        TransactionResponse response = new TransactionResponse();
        response.setTransactionId(tx.getTransactionId());
        Account account = tx.getAccount();
        if (account != null) {
            response.setAccountId(account.getAccountId());
            response.setAccountNumber(account.getAccountNumber());
            Customer customer = account.getCustomer();
            if (customer != null) {
                response.setCustomerId(customer.getCustomerId());
                String first = customer.getFirstName() == null ? "" : customer.getFirstName().trim();
                String last = customer.getLastName() == null ? "" : customer.getLastName().trim();
                response.setCustomerName((first + " " + last).trim());
            }
        }
        response.setTransactionType(tx.getTransactionType());
        response.setAmount(tx.getAmount());
        response.setBalanceAfter(tx.getBalanceAfter());
        response.setCurrencyCode(tx.getCurrencyCode());
        response.setNarration(tx.getNarration());
        response.setCreatedAt(tx.getCreatedAt());
        response.setCreatedBy(tx.getCreatedBy());
        return response;
    }
}
