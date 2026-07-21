package com.bankone.account.dto;

import com.bankone.account.enums.AccountType;
import com.bankone.account.enums.CurrencyCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AccountPolicyResponse {

    private Long policyId;
    private AccountType accountType;
    private CurrencyCode currencyCode;
    private Boolean openingDepositRequired;
    private BigDecimal requiredOpeningDeposit;
    private BigDecimal minimumBalance;
    private Boolean active;
    private LocalDateTime effectiveFrom;
    private LocalDateTime effectiveTo;
    private LocalDateTime createdAt;
    private String createdBy;


    // Generate getters and setters
}