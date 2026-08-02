package com.bankone.transfer.dto;

import com.bankone.beneficiary.enums.BeneficiaryBankType;
import com.bankone.transfer.enums.TransferRequestStatus;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PendingTransferResponse(
        Long transferRequestId,
        Long customerId,
        Long fromAccountId,
        Long toAccountId,
        Long beneficiaryId,
        BigDecimal amount,
        BeneficiaryBankType bankType,
        TransferRequestStatus status,
        String destinationAccountNumber,
        String accountHolderName,
        String ifsc,
        String bankName,
        String narration,
        String approvalReason,
        String requestedBy,
        String resolvedBy,
        String rejectionReason,
        LocalDateTime createdAt,
        LocalDateTime resolvedAt
) implements Serializable {
    private static final long serialVersionUID = 1L;
}
