package com.bankone.account.enums;

public enum CurrencyCode {

    INR(356),
    USD(840),
    EUR(978);

    private final int code;

    CurrencyCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}