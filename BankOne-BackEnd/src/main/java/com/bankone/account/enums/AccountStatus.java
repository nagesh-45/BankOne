package com.bankone.account.enums;

public enum AccountStatus {

    ACTIVE("A"),
    FROZEN("F"),
    DORMANT("D"),
    SUSPENDED("S"),
    CLOSED("C");

    private final String code;

    AccountStatus(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}