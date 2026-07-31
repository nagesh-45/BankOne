package com.bankone.transfer.dto;

import com.bankone.beneficiary.enums.BeneficiaryBankType;
import com.bankone.transfer.enums.TransferRequestStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransferOutcomeResponse(
        String outcome,
        Long transferRequestId,
        TransferRequestStatus status,
        Long fromAccountId,
        Long toAccountId,
        BigDecimal amount,
        BeneficiaryBankType bankType,
        String message,
        LocalDateTime createdAt
) {
}
