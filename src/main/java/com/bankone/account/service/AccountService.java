package com.bankone.account.service;

import com.bankone.account.dto.AccountResponse;
import com.bankone.account.dto.OpenAccountRequest;
import com.bankone.account.dto.UpdateAccountStatusRequest;

import java.util.List;

public interface AccountService {

    AccountResponse openAccount(OpenAccountRequest request);

    List<AccountResponse> getAccountsByCustomerId(Long customerId);

    AccountResponse updateAccountStatus(Long accountId, UpdateAccountStatusRequest request);
}
