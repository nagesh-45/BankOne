package com.bankone.account.util;

import com.bankone.account.enums.AccountType;
import com.bankone.account.enums.CurrencyCode;
import org.springframework.stereotype.Component;

@Component
public class AccountNumberGenerator {
    public String generate(String branchCode,
                           AccountType accountType,
                           Long ordinal,
                           CurrencyCode currencyCode) {
        validateBranchCode(branchCode);
        validateOrdinal(ordinal);

        String typeCode = accountType.getCode();
        String currency = String.format("%03d", currencyCode.getCode());

        if (typeCode == null || typeCode.length() != 2 || !typeCode.matches("\\d{2}")) {
            throw new IllegalArgumentException("Invalid account type code.");
        }

        if (currency == null || currency.length() != 3 || !currency.matches("\\d{3}")) {
            throw new IllegalArgumentException("Invalid currency code.");
        }

        String formattedOrdinal = String.format("%08d", ordinal);
        String base = branchCode + typeCode + formattedOrdinal + currency;
        String checkDigit = calculateCheckDigit(base);
        String accountNumber = base + checkDigit;

        if (!accountNumber.matches("\\d{19}")) {
            throw new IllegalStateException("Generated account number is invalid.");
        }

        return accountNumber;
    }

    private void validateBranchCode(String branchCode) {
        if (branchCode == null || !branchCode.matches("\\d{4}")) {
            throw new IllegalArgumentException("Branch code must be exactly 4 numeric digits.");
        }
    }

    private void validateOrdinal(Long ordinal) {
        if (ordinal == null || ordinal <= 0) {
            throw new IllegalArgumentException("Ordinal must be greater than zero.");
        }

        if (ordinal > 99_999_999L) {
            throw new IllegalArgumentException("Ordinal exceeds maximum supported value.");
        }
    }

    private String calculateCheckDigit(String base) {
        long remainder = 0;

        for (char digit : base.toCharArray()) {
            remainder = (remainder * 10 + Character.getNumericValue(digit)) % 97;
        }

        int checkDigit = 98 - (int) remainder;

        if (checkDigit == 98) {
            checkDigit = 0;
        }

        return String.format("%02d", checkDigit);
    }
}
