package com.bankone.beneficiary.dto;

import com.bankone.beneficiary.enums.BeneficiaryBankType;

import java.time.LocalDateTime;

public record BeneficiaryResponse(
        Long beneficiaryId,
        String nickname,
        BeneficiaryBankType bankType,
        String accountNumber,
        String accountHolderName,
        Long linkedAccountId,
        String ifsc,
        String bankName,
        LocalDateTime createdAt
) {
}
