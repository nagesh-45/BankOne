package com.bankone.portal.service;

import com.bankone.account.dto.AccountResponse;
import com.bankone.account.service.AccountService;
import com.bankone.common.exception.ResourceNotFoundException;
import com.bankone.transaction.dto.TransactionResponse;
import com.bankone.transaction.service.TransactionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PortalAccountService {

    private final PortalCustomerContext portalCustomerContext;
    private final AccountService accountService;
    private final TransactionService transactionService;

    public PortalAccountService(
            PortalCustomerContext portalCustomerContext,
            AccountService accountService,
            TransactionService transactionService
    ) {
        this.portalCustomerContext = portalCustomerContext;
        this.accountService = accountService;
        this.transactionService = transactionService;
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> listMyAccounts() {
        return accountService.getAccountsByCustomerId(portalCustomerContext.requireCustomerId());
    }

    @Transactional(readOnly = true)
    public AccountResponse getMyAccount(Long accountId) {
        AccountResponse account = accountService.getAccountById(accountId);
        assertOwns(account);
        return account;
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getMyTransactions(Long accountId, Pageable pageable) {
        AccountResponse account = accountService.getAccountById(accountId);
        assertOwns(account);
        return transactionService.getByAccountId(accountId, pageable);
    }

    private void assertOwns(AccountResponse account) {
        Long customerId = portalCustomerContext.requireCustomerId();
        if (account.getCustomerId() == null || !account.getCustomerId().equals(customerId)) {
            throw new ResourceNotFoundException("Account not found");
        }
    }
}
