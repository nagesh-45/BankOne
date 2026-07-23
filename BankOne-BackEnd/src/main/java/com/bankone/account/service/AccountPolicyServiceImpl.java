package com.bankone.account.service;

import com.bankone.account.dto.AccountPolicyResponse;
import com.bankone.account.dto.CreateAccountPolicyRequest;
import com.bankone.account.entity.AccountPolicy;
import com.bankone.account.enums.AccountType;
import com.bankone.account.enums.CurrencyCode;
import com.bankone.account.repository.AccountPolicyRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AccountPolicyServiceImpl implements AccountPolicyService {

    private final AccountPolicyRepository accountPolicyRepository;

    public AccountPolicyServiceImpl(AccountPolicyRepository accountPolicyRepository) {
        this.accountPolicyRepository = accountPolicyRepository;
    }

    @Override
    public AccountPolicyResponse createPolicy(CreateAccountPolicyRequest request) {

        AccountPolicy policy = new AccountPolicy();

        policy.setAccountType(request.getAccountType());
        policy.setCurrencyCode(request.getCurrencyCode());
        policy.setOpeningDepositRequired(request.getOpeningDepositRequired());
        policy.setRequiredOpeningDeposit(request.getRequiredOpeningDeposit());
        policy.setMinimumBalance(request.getMinimumBalance());
        policy.setActive(request.getActive());
        policy.setEffectiveFrom(request.getEffectiveFrom());
        policy.setEffectiveTo(request.getEffectiveTo());
        //policy.setCreatedBy(request.getCreatedBy());
        policy.setCreatedAt(LocalDateTime.now());

        AccountPolicy savedPolicy = accountPolicyRepository.save(policy);

        AccountPolicyResponse response = new AccountPolicyResponse();

        response.setPolicyId(savedPolicy.getPolicyId());
        response.setAccountType(savedPolicy.getAccountType());
        response.setCurrencyCode(savedPolicy.getCurrencyCode());
        response.setOpeningDepositRequired(savedPolicy.getOpeningDepositRequired());
        response.setRequiredOpeningDeposit(savedPolicy.getRequiredOpeningDeposit());
        response.setMinimumBalance(savedPolicy.getMinimumBalance());
        response.setActive(savedPolicy.getActive());
        response.setEffectiveFrom(savedPolicy.getEffectiveFrom());
        response.setEffectiveTo(savedPolicy.getEffectiveTo());
        response.setCreatedAt(savedPolicy.getCreatedAt());
        //response.setCreatedBy(savedPolicy.getCreatedBy());

        return response;
    }

    @Override
    public AccountPolicyResponse getActivePolicy(String accountType, String currencyCode) {
        AccountType type = AccountType.valueOf(accountType.trim().toUpperCase());
        CurrencyCode currency = CurrencyCode.valueOf(currencyCode.trim().toUpperCase());

        AccountPolicy policy = accountPolicyRepository
                .findByAccountTypeAndCurrencyCodeAndActiveTrue(type, currency)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No active policy for " + type + " / " + currency));

        AccountPolicyResponse response = new AccountPolicyResponse();
        response.setPolicyId(policy.getPolicyId());
        response.setAccountType(policy.getAccountType());
        response.setCurrencyCode(policy.getCurrencyCode());
        response.setOpeningDepositRequired(policy.getOpeningDepositRequired());
        response.setRequiredOpeningDeposit(policy.getRequiredOpeningDeposit());
        response.setMinimumBalance(policy.getMinimumBalance());
        response.setActive(policy.getActive());
        response.setEffectiveFrom(policy.getEffectiveFrom());
        response.setEffectiveTo(policy.getEffectiveTo());
        response.setCreatedAt(policy.getCreatedAt());
        return response;
    }
}