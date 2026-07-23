package com.bankone.account.service;

import com.bankone.account.entity.AccountPolicy;
import com.bankone.account.enums.AccountType;
import com.bankone.account.enums.CurrencyCode;
import com.bankone.account.repository.AccountPolicyRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class AccountPolicyInitializer implements ApplicationRunner {

    private final AccountPolicyRepository accountPolicyRepository;

    public AccountPolicyInitializer(AccountPolicyRepository accountPolicyRepository) {
        this.accountPolicyRepository = accountPolicyRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        ensurePolicy(AccountType.CURRENT, CurrencyCode.INR,
            true, new BigDecimal("5000.00"), new BigDecimal("1000.00"));
        ensurePolicy(AccountType.SAVINGS, CurrencyCode.INR,
            true, new BigDecimal("1000.00"), new BigDecimal("500.00"));
        ensurePolicy(AccountType.SALARY, CurrencyCode.INR,
            true, new BigDecimal("0.00"), new BigDecimal("0.00"));
        ensurePolicy(AccountType.FIXED_DEPOSIT, CurrencyCode.INR,
            true, new BigDecimal("10000.00"), new BigDecimal("0.00"));
        ensurePolicy(AccountType.RECURRING_DEPOSIT, CurrencyCode.INR,
            true, new BigDecimal("500.00"), new BigDecimal("0.00"));
    }

    private void ensurePolicy(
            AccountType accountType,
            CurrencyCode currencyCode,
            boolean openingDepositRequired,
            BigDecimal requiredOpeningDeposit,
            BigDecimal minimumBalance
    ) {
        if (accountPolicyRepository
                .findByAccountTypeAndCurrencyCodeAndActiveTrue(accountType, currencyCode)
                .isPresent()) {
            return;
        }

        AccountPolicy policy = new AccountPolicy();
        policy.setAccountType(accountType);
        policy.setCurrencyCode(currencyCode);
        policy.setOpeningDepositRequired(openingDepositRequired);
        policy.setRequiredOpeningDeposit(requiredOpeningDeposit);
        policy.setMinimumBalance(minimumBalance);
        policy.setActive(true);
        policy.setEffectiveFrom(LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0));
        accountPolicyRepository.save(policy);
    }
}
