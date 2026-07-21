package com.bankone.account.service;

import com.bankone.account.dto.AccountPolicyResponse;
import com.bankone.account.dto.CreateAccountPolicyRequest;

public interface AccountPolicyService {
    AccountPolicyResponse createPolicy(CreateAccountPolicyRequest request);
}