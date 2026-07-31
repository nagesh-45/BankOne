package com.bankone.audit.service;

import com.bankone.account.entity.Account;
import com.bankone.account.enums.AccountStatus;
import com.bankone.account.repository.AccountRepository;
import com.bankone.audit.domain.AuditAction;
import com.bankone.audit.domain.AuditCategory;
import com.bankone.audit.dto.AuditBackfillResult;
import com.bankone.audit.entity.AuditEventEntity;
import com.bankone.audit.repository.AuditEventRepository;
import com.bankone.beneficiary.entity.Beneficiary;
import com.bankone.beneficiary.repository.BeneficiaryRepository;
import com.bankone.customer.entity.Customer;
import com.bankone.customer.repository.CustomerRepository;
import com.bankone.role.entity.Role;
import com.bankone.role.repository.RoleRepository;
import com.bankone.transaction.entity.Transaction;
import com.bankone.transaction.enums.TransactionType;
import com.bankone.transaction.repository.TransactionRepository;
import com.bankone.transfer.entity.TransferRequestEntity;
import com.bankone.transfer.enums.TransferRequestStatus;
import com.bankone.transfer.repository.TransferRequestRepository;
import com.bankone.user.entity.User;
import com.bankone.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Reconstructs historical activity from domain tables into audit_event.
 * Idempotent. Call via POST /audit/backfill — not on server startup
 * (large load-test datasets would block Liberty HTTP readiness).
 */
@Service
public class AuditBackfillService {

    private static final Logger log = LoggerFactory.getLogger(AuditBackfillService.class);
    private static final String SOURCE = "source=backfill";
    private static final int BATCH_SIZE = 200;

    private final AuditEventRepository auditEventRepository;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransferRequestRepository transferRequestRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TransactionTemplate txTemplate;

    public AuditBackfillService(
            AuditEventRepository auditEventRepository,
            CustomerRepository customerRepository,
            AccountRepository accountRepository,
            TransactionRepository transactionRepository,
            TransferRequestRepository transferRequestRepository,
            BeneficiaryRepository beneficiaryRepository,
            UserRepository userRepository,
            RoleRepository roleRepository,
            PlatformTransactionManager transactionManager
    ) {
        this.auditEventRepository = auditEventRepository;
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.transferRequestRepository = transferRequestRepository;
        this.beneficiaryRepository = beneficiaryRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.txTemplate = new TransactionTemplate(transactionManager);
    }

    public AuditBackfillResult backfill() {
        Set<String> existing = loadExistingKeys();
        AuditBackfillResult result = new AuditBackfillResult();
        result.addInserted("customers", backfillCustomers(result, existing));
        result.addInserted("accounts", backfillAccounts(result, existing));
        result.addInserted("transactions", backfillTransactions(result, existing));
        result.addInserted("transferRequests", backfillTransferRequests(result, existing));
        result.addInserted("beneficiaries", backfillBeneficiaries(result, existing));
        result.addInserted("users", backfillUsers(result, existing));
        result.addInserted("roles", backfillRoles(result, existing));
        log.info(
                "Audit backfill complete: inserted={}, skipped={}, bySource={}",
                result.getInserted(),
                result.getSkipped(),
                result.getInsertedBySource()
        );
        return result;
    }

    private Set<String> loadExistingKeys() {
        return txTemplate.execute(status -> {
            Set<String> keys = new HashSet<>();
            for (AuditEventEntity e : auditEventRepository.findAll()) {
                if (e.getAction() != null && e.getTargetType() != null && e.getTargetId() != null) {
                    keys.add(key(e.getAction(), e.getTargetType(), e.getTargetId()));
                }
            }
            return keys;
        });
    }

    private int backfillCustomers(AuditBackfillResult result, Set<String> existing) {
        int inserted = 0;
        List<AuditEventEntity> batch = new ArrayList<>();
        for (Customer c : customerRepository.findAll()) {
            AuditEventEntity event = build(
                    AuditCategory.CUSTOMER,
                    AuditAction.CUSTOMER_CREATE,
                    null,
                    "CUSTOMER",
                    String.valueOf(c.getCustomerId()),
                    "Customer created: " + safe(c.getFirstName()) + " " + safe(c.getLastName()),
                    SOURCE + ", status=" + c.getStatus(),
                    c.getCreatedAt(),
                    existing
            );
            if (event == null) {
                result.addSkipped(1);
                continue;
            }
            batch.add(event);
            if (batch.size() >= BATCH_SIZE) {
                inserted += flush(batch);
            }
        }
        inserted += flush(batch);
        return inserted;
    }

    private int backfillAccounts(AuditBackfillResult result, Set<String> existing) {
        int inserted = 0;
        List<AuditEventEntity> batch = new ArrayList<>();
        for (Account a : accountRepository.findAllWithCustomer()) {
            String customerId = a.getCustomer() == null || a.getCustomer().getCustomerId() == null
                    ? null
                    : String.valueOf(a.getCustomer().getCustomerId());
            AuditEventEntity open = build(
                    AuditCategory.ACCOUNT,
                    AuditAction.ACCOUNT_OPEN,
                    a.getCreatedBy(),
                    "ACCOUNT",
                    String.valueOf(a.getAccountId()),
                    "Opened account " + a.getAccountNumber(),
                    SOURCE + ", customerId=" + customerId + ", type=" + a.getAccountType(),
                    a.getCreatedAt(),
                    existing
            );
            if (open == null) {
                result.addSkipped(1);
            } else {
                batch.add(open);
            }

            if (AccountStatus.CLOSED.name().equals(a.getStatus()) && a.getClosedAt() != null) {
                AuditEventEntity closed = build(
                        AuditCategory.ACCOUNT,
                        AuditAction.ACCOUNT_STATUS_CHANGE,
                        a.getClosedBy(),
                        "ACCOUNT_STATUS",
                        a.getAccountId() + ":CLOSED",
                        "Account status → CLOSED (" + a.getAccountNumber() + ")",
                        SOURCE,
                        a.getClosedAt(),
                        existing
                );
                if (closed == null) {
                    result.addSkipped(1);
                } else {
                    batch.add(closed);
                }
            }

            if (batch.size() >= BATCH_SIZE) {
                inserted += flush(batch);
            }
        }
        inserted += flush(batch);
        return inserted;
    }

    private int backfillTransactions(AuditBackfillResult result, Set<String> existing) {
        int inserted = 0;
        List<AuditEventEntity> batch = new ArrayList<>();
        for (Transaction tx : transactionRepository.findAllWithAccount()) {
            String narration = tx.getNarration() == null ? "" : tx.getNarration();
            if (narration.startsWith("Transfer from")) {
                result.addSkipped(1);
                continue;
            }

            String action;
            String summary;
            if (narration.startsWith("Transfer to")) {
                action = AuditAction.ACCOUNT_TRANSFER;
                summary = "Transfer " + tx.getAmount() + " (" + narration + ")";
            } else if (tx.getTransactionType() == TransactionType.CREDIT) {
                action = AuditAction.ACCOUNT_DEPOSIT;
                summary = "Deposit " + tx.getAmount()
                        + (tx.getAccount() == null ? "" : " to " + tx.getAccount().getAccountNumber());
            } else {
                action = AuditAction.ACCOUNT_WITHDRAW;
                summary = "Withdraw " + tx.getAmount()
                        + (tx.getAccount() == null ? "" : " from " + tx.getAccount().getAccountNumber());
            }

            LocalDateTime when = tx.getCreatedAt() == null
                    ? LocalDateTime.now()
                    : LocalDateTime.ofInstant(tx.getCreatedAt(), ZoneId.systemDefault());

            AuditEventEntity event = build(
                    AuditCategory.ACCOUNT,
                    action,
                    tx.getCreatedBy(),
                    "TRANSACTION",
                    String.valueOf(tx.getTransactionId()),
                    summary,
                    SOURCE + (narration.isBlank() ? "" : ", narration=" + narration),
                    when,
                    existing
            );
            if (event == null) {
                result.addSkipped(1);
            } else {
                batch.add(event);
            }
            if (batch.size() >= BATCH_SIZE) {
                inserted += flush(batch);
            }
        }
        inserted += flush(batch);
        return inserted;
    }

    private int backfillTransferRequests(AuditBackfillResult result, Set<String> existing) {
        int inserted = 0;
        List<AuditEventEntity> batch = new ArrayList<>();
        for (TransferRequestEntity t : transferRequestRepository.findAll()) {
            AuditEventEntity requested = build(
                    AuditCategory.PORTAL,
                    AuditAction.TRANSFER_REQUESTED,
                    t.getRequestedBy(),
                    "TRANSFER_REQUEST",
                    String.valueOf(t.getTransferRequestId()),
                    "Portal transfer requested amount=" + t.getAmount(),
                    SOURCE + ", to=" + t.getDestinationAccountNumber()
                            + ", bankType=" + t.getBankType()
                            + ", status=" + t.getStatus(),
                    t.getCreatedAt(),
                    existing
            );
            if (requested == null) {
                result.addSkipped(1);
            } else {
                batch.add(requested);
            }

            if (t.getStatus() != TransferRequestStatus.PENDING && t.getResolvedAt() != null) {
                String resolveAction = t.getStatus() == TransferRequestStatus.REJECTED
                        ? AuditAction.TRANSFER_REJECTED
                        : AuditAction.TRANSFER_APPROVED;
                String label = t.getStatus() == TransferRequestStatus.EXECUTED
                        ? "Executed"
                        : t.getStatus() == TransferRequestStatus.REJECTED ? "Rejected" : "Approved";

                AuditEventEntity resolved = build(
                        AuditCategory.TRANSFER,
                        resolveAction,
                        t.getResolvedBy(),
                        "TRANSFER_REQUEST",
                        t.getTransferRequestId() + ":" + resolveAction,
                        label + " transfer request #" + t.getTransferRequestId()
                                + " amount=" + t.getAmount(),
                        SOURCE + (t.getRejectionReason() == null ? "" : ", reason=" + t.getRejectionReason()),
                        t.getResolvedAt(),
                        existing
                );
                if (resolved == null) {
                    result.addSkipped(1);
                } else {
                    batch.add(resolved);
                }
            }

            if (batch.size() >= BATCH_SIZE) {
                inserted += flush(batch);
            }
        }
        inserted += flush(batch);
        return inserted;
    }

    private int backfillBeneficiaries(AuditBackfillResult result, Set<String> existing) {
        int inserted = 0;
        List<AuditEventEntity> batch = new ArrayList<>();
        for (Beneficiary b : beneficiaryRepository.findAll()) {
            AuditEventEntity created = build(
                    AuditCategory.PORTAL,
                    AuditAction.BENEFICIARY_CREATE,
                    null,
                    "BENEFICIARY",
                    String.valueOf(b.getBeneficiaryId()),
                    "Beneficiary added: " + b.getNickname(),
                    SOURCE + ", account=" + b.getAccountNumber()
                            + ", bankType=" + b.getBankType()
                            + ", customerId=" + b.getCustomerId(),
                    b.getCreatedAt(),
                    existing
            );
            if (created == null) {
                result.addSkipped(1);
            } else {
                batch.add(created);
            }

            if (!Boolean.TRUE.equals(b.getActive())) {
                AuditEventEntity deleted = build(
                        AuditCategory.PORTAL,
                        AuditAction.BENEFICIARY_DELETE,
                        null,
                        "BENEFICIARY_INACTIVE",
                        String.valueOf(b.getBeneficiaryId()),
                        "Beneficiary deactivated: " + b.getNickname(),
                        SOURCE + " (deactivation time unknown — using create time)",
                        b.getCreatedAt(),
                        existing
                );
                if (deleted == null) {
                    result.addSkipped(1);
                } else {
                    batch.add(deleted);
                }
            }
            if (batch.size() >= BATCH_SIZE) {
                inserted += flush(batch);
            }
        }
        inserted += flush(batch);
        return inserted;
    }

    private int backfillUsers(AuditBackfillResult result, Set<String> existing) {
        int inserted = 0;
        List<AuditEventEntity> batch = new ArrayList<>();
        for (User u : userRepository.findAll()) {
            boolean portal = u.getCustomerId() != null;
            AuditEventEntity event = build(
                    portal ? AuditCategory.PORTAL : AuditCategory.STAFF,
                    AuditAction.USER_CREATE,
                    null,
                    "USER",
                    String.valueOf(u.getUserId()),
                    (portal ? "Portal login created: " : "Staff user created: ") + u.getUsername(),
                    SOURCE + (portal ? ", customerId=" + u.getCustomerId() : ""),
                    u.getCreatedAt(),
                    existing
            );
            if (event == null) {
                result.addSkipped(1);
            } else {
                batch.add(event);
            }
            if (batch.size() >= BATCH_SIZE) {
                inserted += flush(batch);
            }
        }
        inserted += flush(batch);
        return inserted;
    }

    private int backfillRoles(AuditBackfillResult result, Set<String> existing) {
        int inserted = 0;
        List<AuditEventEntity> batch = new ArrayList<>();
        for (Role r : roleRepository.findAll()) {
            AuditEventEntity event = build(
                    AuditCategory.ROLE,
                    AuditAction.ROLE_CREATE,
                    null,
                    "ROLE",
                    String.valueOf(r.getRoleId()),
                    "Role created: " + r.getRoleName(),
                    SOURCE,
                    r.getCreatedAt(),
                    existing
            );
            if (event == null) {
                result.addSkipped(1);
            } else {
                batch.add(event);
            }
        }
        inserted += flush(batch);
        return inserted;
    }

    private AuditEventEntity build(
            AuditCategory category,
            String action,
            String actorUsername,
            String targetType,
            String targetId,
            String summary,
            String details,
            LocalDateTime createdAt,
            Set<String> existing
    ) {
        if (targetId == null || targetType == null) {
            return null;
        }
        String k = key(action, targetType, targetId);
        if (existing.contains(k)) {
            return null;
        }
        existing.add(k);

        AuditEventEntity event = new AuditEventEntity();
        event.setCategory(category);
        event.setAction(action);
        event.setActorUsername(blankToNull(actorUsername));
        event.setTargetType(targetType);
        event.setTargetId(targetId);
        event.setSummary(trim(summary, 500));
        event.setDetails(trim(details, 2000));
        event.setSuccess(true);
        event.setCreatedAt(createdAt != null ? createdAt : LocalDateTime.now());
        return event;
    }

    private int flush(List<AuditEventEntity> batch) {
        if (batch.isEmpty()) {
            return 0;
        }
        List<AuditEventEntity> copy = new ArrayList<>(batch);
        batch.clear();
        Integer saved = txTemplate.execute(status -> {
            auditEventRepository.saveAll(copy);
            return copy.size();
        });
        return saved == null ? 0 : saved;
    }

    private static String key(String action, String targetType, String targetId) {
        return action + "|" + targetType + "|" + targetId;
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private static String trim(String value, int max) {
        if (value == null) {
            return null;
        }
        String t = value.trim();
        return t.length() <= max ? t : t.substring(0, max);
    }
}
