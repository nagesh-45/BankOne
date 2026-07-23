package com.bankone.account.dto;

import com.bankone.account.enums.AccountType;
import com.bankone.account.enums.CurrencyCode;

import java.math.BigDecimal;

public class OpenAccountRequest {

    private Long customerId;

    private String branchCode;

    private AccountType accountType;

    private CurrencyCode currencyCode;

    private BigDecimal openingDeposit;

    private String createdBy;

    public OpenAccountRequest() {
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
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

    public BigDecimal getOpeningDeposit() {
        return openingDeposit;
    }

    public void setOpeningDeposit(BigDecimal openingDeposit) {
        this.openingDeposit = openingDeposit;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}