package com.bankone.account.enums;

public enum AccountType {

    SAVINGS("01"),
    CURRENT("02"),
    SALARY("03"),
    FIXED_DEPOSIT("04"),
    RECURRING_DEPOSIT("05"),
    LOAN("06");

    private final String code;

    AccountType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}