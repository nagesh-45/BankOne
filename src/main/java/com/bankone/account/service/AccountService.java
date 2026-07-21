package com.bankone.account.service;

import com.bankone.account.dto.AccountResponse;
import com.bankone.account.dto.OpenAccountRequest;

public interface AccountService {

    AccountResponse openAccount(OpenAccountRequest request);

}