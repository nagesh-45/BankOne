package com.bankone.account.service;

import com.bankone.account.dto.AccountResponse;
import com.bankone.account.dto.OpenAccountRequest;
import com.bankone.account.dto.UpdateAccountStatusRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.bankone.account.dto.DepositRequest;
import java.util.List;
import com.bankone.account.dto.WithdrawRequest;
import com.bankone.account.dto.TransferRequest;
public interface AccountService {

    AccountResponse openAccount(OpenAccountRequest request);

    List<AccountResponse> getAccountsByCustomerId(Long customerId);
    AccountResponse getAccountById(Long accountId);
    Page<AccountResponse> getAccountsByCustomerId(Long customerId, Pageable pageable);
    AccountResponse deposit(Long accountId, DepositRequest request);
    AccountResponse updateAccountStatus(Long accountId, UpdateAccountStatusRequest request);
    Page<AccountResponse> searchAccounts(String search, Pageable pageable);
    AccountResponse withdraw(Long accountId, WithdrawRequest request);
    AccountResponse transfer(Long fromAccountId, TransferRequest request);
}
