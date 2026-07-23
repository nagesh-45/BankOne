package com.bankone.account.repository;

import com.bankone.account.entity.AccountPolicy;
import com.bankone.account.enums.AccountType;
import com.bankone.account.enums.CurrencyCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountPolicyRepository extends JpaRepository<AccountPolicy, Long> {

    Optional<AccountPolicy> findByAccountTypeAndCurrencyCodeAndActiveTrue(
            AccountType accountType,
            CurrencyCode currencyCode
    );
}