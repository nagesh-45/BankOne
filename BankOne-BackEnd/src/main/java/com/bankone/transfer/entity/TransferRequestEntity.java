package com.bankone.transfer.entity;

import com.bankone.beneficiary.enums.BeneficiaryBankType;
import com.bankone.transfer.enums.TransferRequestStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "transfer_request")
@SequenceGenerator(name = "transfer_request_seq", sequenceName = "transfer_request_seq", allocationSize = 1)
public class TransferRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "transfer_request_seq")
    @Column(name = "transfer_request_id")
    private Long transferRequestId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "from_account_id", nullable = false)
    private Long fromAccountId;

    @Column(name = "to_account_id")
    private Long toAccountId;

    @Column(name = "beneficiary_id")
    private Long beneficiaryId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "bank_type", nullable = false, length = 20)
    private BeneficiaryBankType bankType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TransferRequestStatus status;

    @Column(name = "destination_account_number", length = 34)
    private String destinationAccountNumber;

    @Column(name = "account_holder_name", length = 120)
    private String accountHolderName;

    @Column(name = "ifsc", length = 20)
    private String ifsc;

    @Column(name = "bank_name", length = 120)
    private String bankName;

    @Column(name = "narration", length = 255)
    private String narration;

    @Column(name = "approval_reason", length = 255)
    private String approvalReason;

    @Column(name = "requested_by", length = 50)
    private String requestedBy;

    @Column(name = "resolved_by", length = 50)
    private String resolvedBy;

    @Column(name = "rejection_reason", length = 255)
    private String rejectionReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
