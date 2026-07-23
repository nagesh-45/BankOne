package com.bankone.account.service;

import com.bankone.account.dto.AccountResponse;
import com.bankone.account.dto.OpenAccountRequest;
import com.bankone.account.dto.UpdateAccountStatusRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.bankone.account.dto.DepositRequest;
import java.util.List;

public interface AccountService {

    AccountResponse openAccount(OpenAccountRequest request);

    List<AccountResponse> getAccountsByCustomerId(Long customerId);

    Page<AccountResponse> getAccountsByCustomerId(Long customerId, Pageable pageable);
    AccountResponse deposit(Long accountId, DepositRequest request);
    AccountResponse updateAccountStatus(Long accountId, UpdateAccountStatusRequest request);
    Page<AccountResponse> searchAccounts(String search, Pageable pageable);
}
