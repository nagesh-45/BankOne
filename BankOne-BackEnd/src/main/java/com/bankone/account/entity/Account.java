package com.bankone.account.entity;

import com.bankone.customer.entity.Customer;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "account")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long accountId;

    @Column(name = "account_number", nullable = false, unique = true)
    private String accountNumber;

    @Column(name = "branch_code", nullable = false)
    private String branchCode;

    @Column(name = "account_type", nullable = false)
    private String accountType;

    @Column(name = "ordinal")
    private Integer ordinal;

    @Column(name = "currency_code", nullable = false)
    private String currencyCode;

    @Column(name = "check_digit")
    private Integer checkDigit;

    @Column(name = "available_balance", nullable = false)
    private BigDecimal availableBalance;

    @Column(name = "ledger_balance", nullable = false)
    private BigDecimal ledgerBalance;

    @Column(name = "debit_count")
    private Integer debitCount;

    @Column(name = "credit_count")
    private Integer creditCount;

    @Column(name = "last_debit_at")
    private LocalDateTime lastDebitAt;

    @Column(name = "last_credit_at")
    private LocalDateTime lastCreditAt;

    @Column(name = "last_transaction_at")
    private LocalDateTime lastTransactionAt;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "activated_at")
    private LocalDateTime activatedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "closed_by")
    private String closedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;
}