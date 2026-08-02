package com.bankone.transfer.service;

import com.bankone.account.dto.TransferRequest;
import com.bankone.account.service.AccountService;
import com.bankone.audit.domain.AuditAction;
import com.bankone.audit.domain.AuditCategory;
import com.bankone.audit.service.AuditEventService;
import com.bankone.beneficiary.enums.BeneficiaryBankType;
import com.bankone.cache.CacheNames;
import com.bankone.common.exception.BadRequestException;
import com.bankone.common.exception.ResourceNotFoundException;
import com.bankone.transfer.dto.PendingTransferResponse;
import com.bankone.transfer.dto.ResolveTransferRequest;
import com.bankone.transfer.entity.TransferRequestEntity;
import com.bankone.transfer.enums.TransferRequestStatus;
import com.bankone.transfer.repository.TransferRequestRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class TransferApprovalService {

    private static final Set<TransferRequestStatus> RESOLVED = Set.of(
            TransferRequestStatus.APPROVED,
            TransferRequestStatus.REJECTED,
            TransferRequestStatus.EXECUTED
    );

    private final TransferRequestRepository transferRequestRepository;
    private final AccountService accountService;
    private final AuditEventService auditEventService;

    public TransferApprovalService(
            TransferRequestRepository transferRequestRepository,
            AccountService accountService,
            AuditEventService auditEventService
    ) {
        this.transferRequestRepository = transferRequestRepository;
        this.accountService = accountService;
        this.auditEventService = auditEventService;
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheNames.TRANSFERS, key = "'pending'")
    public List<PendingTransferResponse> listPending() {
        return transferRequestRepository.findByStatusOrderByCreatedAtAsc(TransferRequestStatus.PENDING)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /** History for the currently logged-in employee (what they approved/rejected). */
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheNames.TRANSFERS, key = "'history:' + T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName()")
    public List<PendingTransferResponse> listMyHistory() {
        String staff = SecurityContextHolder.getContext().getAuthentication().getName();
        return transferRequestRepository
                .findByResolvedByIgnoreCaseAndStatusInOrderByResolvedAtDesc(staff, RESOLVED)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /** Full approval audit trail — Admin / Manager / Auditor. */
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheNames.TRANSFERS, key = "'audit'")
    public List<PendingTransferResponse> listAuditHistory() {
        return transferRequestRepository.findByStatusInOrderByResolvedAtDesc(RESOLVED)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.TRANSFERS, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.ACCOUNTS, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.TRANSACTIONS, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.REPORTS, allEntries = true)
    })
    public PendingTransferResponse approve(Long id) {
        TransferRequestEntity pending = requirePending(id);
        String staff = SecurityContextHolder.getContext().getAuthentication().getName();

        if (pending.getBankType() == BeneficiaryBankType.SAME_BANK) {
            if (pending.getToAccountId() == null) {
                throw new BadRequestException("Pending same-bank transfer is missing destination account");
            }
            TransferRequest req = new TransferRequest();
            req.setToAccountId(pending.getToAccountId());
            req.setAmount(pending.getAmount());
            accountService.transfer(pending.getFromAccountId(), req);
            pending.setStatus(TransferRequestStatus.EXECUTED);
        } else {
            // External transfer: record approval; no ledger move outside BankOne yet.
            pending.setStatus(TransferRequestStatus.APPROVED);
        }

        pending.setResolvedBy(staff);
        pending.setResolvedAt(LocalDateTime.now());
        PendingTransferResponse response = toResponse(transferRequestRepository.save(pending));
        auditEventService.record(
                AuditCategory.TRANSFER,
                AuditAction.TRANSFER_APPROVED,
                "TRANSFER_REQUEST",
                String.valueOf(pending.getTransferRequestId()),
                "Approved transfer request #" + pending.getTransferRequestId()
                        + " amount=" + pending.getAmount(),
                "status=" + pending.getStatus() + ", bankType=" + pending.getBankType(),
                true
        );
        return response;
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.TRANSFERS, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.REPORTS, allEntries = true)
    })
    public PendingTransferResponse reject(Long id, ResolveTransferRequest request) {
        TransferRequestEntity pending = requirePending(id);
        String staff = SecurityContextHolder.getContext().getAuthentication().getName();
        pending.setStatus(TransferRequestStatus.REJECTED);
        pending.setResolvedBy(staff);
        pending.setResolvedAt(LocalDateTime.now());
        if (request != null && request.getRejectionReason() != null) {
            pending.setRejectionReason(request.getRejectionReason().trim());
        }
        PendingTransferResponse response = toResponse(transferRequestRepository.save(pending));
        auditEventService.record(
                AuditCategory.TRANSFER,
                AuditAction.TRANSFER_REJECTED,
                "TRANSFER_REQUEST",
                String.valueOf(pending.getTransferRequestId()),
                "Rejected transfer request #" + pending.getTransferRequestId(),
                pending.getRejectionReason(),
                true
        );
        return response;
    }

    private TransferRequestEntity requirePending(Long id) {
        TransferRequestEntity pending = transferRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transfer request not found"));
        if (pending.getStatus() != TransferRequestStatus.PENDING) {
            throw new BadRequestException("Transfer request is not pending");
        }
        return pending;
    }

    private PendingTransferResponse toResponse(TransferRequestEntity t) {
        return new PendingTransferResponse(
                t.getTransferRequestId(),
                t.getCustomerId(),
                t.getFromAccountId(),
                t.getToAccountId(),
                t.getBeneficiaryId(),
                t.getAmount(),
                t.getBankType(),
                t.getStatus(),
                t.getDestinationAccountNumber(),
                t.getAccountHolderName(),
                t.getIfsc(),
                t.getBankName(),
                t.getNarration(),
                t.getApprovalReason(),
                t.getRequestedBy(),
                t.getResolvedBy(),
                t.getRejectionReason(),
                t.getCreatedAt(),
                t.getResolvedAt()
        );
    }
}
