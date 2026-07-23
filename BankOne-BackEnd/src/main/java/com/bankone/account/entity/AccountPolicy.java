package com.bankone.account.entity;

import com.bankone.account.enums.AccountType;
import com.bankone.account.enums.CurrencyCode;
import com.bankone.common.entity.AuditableEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "account_policy",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_account_policy_type_currency",
                        columnNames = {"account_type", "currency_code"}
                )
        }
)
public class AccountPolicy extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long policyId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType accountType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CurrencyCode currencyCode;

    @Column(nullable = false)
    private Boolean openingDepositRequired;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal requiredOpeningDeposit;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal minimumBalance;

    @Column(nullable = false)
    private Boolean active;

    @Column(nullable = false)
    private LocalDateTime effectiveFrom;

    private LocalDateTime effectiveTo;

    public AccountPolicy() {
    }

    public Long getPolicyId() {
        return policyId;
    }

    public void setPolicyId(Long policyId) {
        this.policyId = policyId;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public CurrencyCode getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(CurrencyCode currencyCode) {
        this.currencyCode = currencyCode;
    }

    public Boolean getOpeningDepositRequired() {
        return openingDepositRequired;
    }

    public void setOpeningDepositRequired(Boolean openingDepositRequired) {
        this.openingDepositRequired = openingDepositRequired;
    }

    public BigDecimal getRequiredOpeningDeposit() {
        return requiredOpeningDeposit;
    }

    public void setRequiredOpeningDeposit(BigDecimal requiredOpeningDeposit) {
        this.requiredOpeningDeposit = requiredOpeningDeposit;
    }

    public BigDecimal getMinimumBalance() {
        return minimumBalance;
    }

    public void setMinimumBalance(BigDecimal minimumBalance) {
        this.minimumBalance = minimumBalance;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public LocalDateTime getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(LocalDateTime effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public LocalDateTime getEffectiveTo() {
        return effectiveTo;
    }

    public void setEffectiveTo(LocalDateTime effectiveTo) {
        this.effectiveTo = effectiveTo;
    }




}