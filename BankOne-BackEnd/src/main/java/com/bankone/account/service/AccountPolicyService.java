package com.bankone.account.service;

import com.bankone.account.dto.AccountPolicyResponse;
import com.bankone.account.dto.CreateAccountPolicyRequest;
import com.bankone.account.dto.UpdateAccountPolicyRequest;

import java.util.List;

public interface AccountPolicyService {
    AccountPolicyResponse createPolicy(CreateAccountPolicyRequest request);

    AccountPolicyResponse getActivePolicy(String accountType, String currencyCode);

    List<AccountPolicyResponse> listAll();

    AccountPolicyResponse updatePolicy(Long policyId, UpdateAccountPolicyRequest request);
}
