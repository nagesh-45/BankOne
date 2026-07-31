package com.bankone.beneficiary.entity;

import com.bankone.beneficiary.enums.BeneficiaryBankType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "beneficiary")
@SequenceGenerator(name = "beneficiary_seq", sequenceName = "beneficiary_seq", allocationSize = 1)
public class Beneficiary {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "beneficiary_seq")
    @Column(name = "beneficiary_id")
    private Long beneficiaryId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "nickname", nullable = false, length = 100)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(name = "bank_type", nullable = false, length = 20)
    private BeneficiaryBankType bankType;

    @Column(name = "account_number", nullable = false, length = 34)
    private String accountNumber;

    @Column(name = "account_holder_name", nullable = false, length = 120)
    private String accountHolderName;

    /** Resolved BankOne account id when SAME_BANK. */
    @Column(name = "linked_account_id")
    private Long linkedAccountId;

    @Column(name = "ifsc", length = 20)
    private String ifsc;

    @Column(name = "bank_name", length = 120)
    private String bankName;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
