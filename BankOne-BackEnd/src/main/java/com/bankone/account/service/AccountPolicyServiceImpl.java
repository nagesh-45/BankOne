package com.bankone.account.service;

import com.bankone.account.dto.AccountPolicyResponse;
import com.bankone.account.dto.CreateAccountPolicyRequest;
import com.bankone.account.dto.UpdateAccountPolicyRequest;
import com.bankone.account.entity.AccountPolicy;
import com.bankone.account.enums.AccountType;
import com.bankone.account.enums.CurrencyCode;
import com.bankone.account.repository.AccountPolicyRepository;
import com.bankone.audit.domain.AuditAction;
import com.bankone.audit.domain.AuditCategory;
import com.bankone.audit.service.AuditEventService;
import com.bankone.cache.CacheNames;
import com.bankone.common.exception.ResourceNotFoundException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AccountPolicyServiceImpl implements AccountPolicyService {

    private final AccountPolicyRepository accountPolicyRepository;
    private final AuditEventService auditEventService;

    public AccountPolicyServiceImpl(
            AccountPolicyRepository accountPolicyRepository,
            AuditEventService auditEventService
    ) {
        this.accountPolicyRepository = accountPolicyRepository;
        this.auditEventService = auditEventService;
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = CacheNames.POLICIES, allEntries = true)
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
        return toResponse(accountPolicyRepository.save(policy));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheNames.POLICIES, key = "'active:' + #accountType + ':' + #currencyCode")
    public AccountPolicyResponse getActivePolicy(String accountType, String currencyCode) {
        AccountType type = AccountType.valueOf(accountType.trim().toUpperCase());
        CurrencyCode currency = CurrencyCode.valueOf(currencyCode.trim().toUpperCase());

        AccountPolicy policy = accountPolicyRepository
                .findByAccountTypeAndCurrencyCodeAndActiveTrue(type, currency)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No active policy for " + type + " / " + currency));

        return toResponse(policy);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheNames.POLICIES, key = "'all'")
    public List<AccountPolicyResponse> listAll() {
        return accountPolicyRepository.findAllByOrderByAccountTypeAscCurrencyCodeAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = CacheNames.POLICIES, allEntries = true)
    public AccountPolicyResponse updatePolicy(Long policyId, UpdateAccountPolicyRequest request) {
        AccountPolicy policy = accountPolicyRepository.findById(policyId)
                .orElseThrow(() -> new ResourceNotFoundException("Account policy not found"));

        policy.setOpeningDepositRequired(request.getOpeningDepositRequired());
        policy.setRequiredOpeningDeposit(request.getRequiredOpeningDeposit());
        policy.setMinimumBalance(request.getMinimumBalance());
        policy.setActive(request.getActive());
        policy.setEffectiveFrom(request.getEffectiveFrom());
        policy.setEffectiveTo(request.getEffectiveTo());

        AccountPolicyResponse response = toResponse(accountPolicyRepository.save(policy));
        auditEventService.record(
                AuditCategory.POLICY,
                AuditAction.POLICY_UPDATE,
                "ACCOUNT_POLICY",
                String.valueOf(response.getPolicyId()),
                "Policy updated: " + response.getAccountType() + "/" + response.getCurrencyCode(),
                "minBalance=" + response.getMinimumBalance()
                        + ", openingRequired=" + response.getOpeningDepositRequired()
                        + ", active=" + response.getActive(),
                true
        );
        return response;
    }

    private AccountPolicyResponse toResponse(AccountPolicy policy) {
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
