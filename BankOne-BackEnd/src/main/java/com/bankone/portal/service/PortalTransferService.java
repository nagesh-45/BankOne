package com.bankone.portal.service;

import com.bankone.account.dto.TransferRequest;
import com.bankone.account.entity.Account;
import com.bankone.account.repository.AccountRepository;
import com.bankone.account.service.AccountService;
import com.bankone.audit.domain.AuditAction;
import com.bankone.audit.domain.AuditCategory;
import com.bankone.audit.service.AuditEventService;
import com.bankone.beneficiary.entity.Beneficiary;
import com.bankone.beneficiary.enums.BeneficiaryBankType;
import com.bankone.beneficiary.repository.BeneficiaryRepository;
import com.bankone.common.exception.BadRequestException;
import com.bankone.common.exception.ResourceNotFoundException;
import com.bankone.customer.entity.Customer;
import com.bankone.customer.repository.CustomerRepository;
import com.bankone.portal.dto.PortalTransferRequest;
import com.bankone.transfer.dto.TransferOutcomeResponse;
import com.bankone.transfer.entity.TransferRequestEntity;
import com.bankone.transfer.enums.TransferRequestStatus;
import com.bankone.transfer.repository.TransferRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

@Service
public class PortalTransferService {

    private final PortalCustomerContext portalCustomerContext;
    private final PortalAccountService portalAccountService;
    private final AccountRepository accountRepository;
    private final AccountService accountService;
    private final CustomerRepository customerRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final TransferRequestRepository transferRequestRepository;
    private final AuditEventService auditEventService;

    public PortalTransferService(
            PortalCustomerContext portalCustomerContext,
            PortalAccountService portalAccountService,
            AccountRepository accountRepository,
            AccountService accountService,
            CustomerRepository customerRepository,
            BeneficiaryRepository beneficiaryRepository,
            TransferRequestRepository transferRequestRepository,
            AuditEventService auditEventService
    ) {
        this.portalCustomerContext = portalCustomerContext;
        this.portalAccountService = portalAccountService;
        this.accountRepository = accountRepository;
        this.accountService = accountService;
        this.customerRepository = customerRepository;
        this.beneficiaryRepository = beneficiaryRepository;
        this.transferRequestRepository = transferRequestRepository;
        this.auditEventService = auditEventService;
    }

    @Transactional
    public TransferOutcomeResponse transfer(Long fromAccountId, PortalTransferRequest request) {
        Long customerId = portalCustomerContext.requireCustomerId();
        // Ensures ownership
        portalAccountService.getMyAccount(fromAccountId);

        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Transfer amount must be greater than zero");
        }

        boolean hasBene = request.getBeneficiaryId() != null;
        boolean hasQuick = StringUtils.hasText(request.getToAccountNumber());
        if (hasBene == hasQuick) {
            throw new BadRequestException("Provide either beneficiaryId or toAccountNumber");
        }

        Destination dest = hasBene
                ? fromBeneficiary(customerId, request.getBeneficiaryId())
                : fromAccountNumber(request.getToAccountNumber().trim());

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        boolean needsApproval = needsApproval(customer, dest.bankType(), request.getAmount());
        if (needsApproval) {
            TransferRequestEntity pending = new TransferRequestEntity();
            pending.setCustomerId(customerId);
            pending.setFromAccountId(fromAccountId);
            pending.setToAccountId(dest.toAccountId());
            pending.setBeneficiaryId(dest.beneficiaryId());
            pending.setAmount(request.getAmount());
            pending.setBankType(dest.bankType());
            pending.setStatus(TransferRequestStatus.PENDING);
            pending.setDestinationAccountNumber(dest.accountNumber());
            pending.setAccountHolderName(dest.holderName());
            pending.setIfsc(dest.ifsc());
            pending.setBankName(dest.bankName());
            pending.setNarration(request.getNarration());
            pending.setApprovalReason(approvalReason(customer, dest.bankType(), request.getAmount()));
            pending.setRequestedBy(portalCustomerContext.currentUser().getUsername());
            TransferRequestEntity saved = transferRequestRepository.save(pending);
            auditEventService.record(
                    AuditCategory.PORTAL,
                    AuditAction.TRANSFER_REQUESTED,
                    "TRANSFER_REQUEST",
                    String.valueOf(saved.getTransferRequestId()),
                    "Portal transfer pending approval amount=" + request.getAmount(),
                    "to=" + dest.accountNumber() + ", bankType=" + dest.bankType(),
                    true
            );
            return new TransferOutcomeResponse(
                    "PENDING_APPROVAL",
                    saved.getTransferRequestId(),
                    saved.getStatus(),
                    fromAccountId,
                    dest.toAccountId(),
                    request.getAmount(),
                    dest.bankType(),
                    saved.getApprovalReason(),
                    saved.getCreatedAt()
            );
        }

        if (dest.bankType() != BeneficiaryBankType.SAME_BANK || dest.toAccountId() == null) {
            throw new BadRequestException("Other-bank transfers always require staff approval");
        }

        TransferRequest staffReq = new TransferRequest();
        staffReq.setToAccountId(dest.toAccountId());
        staffReq.setAmount(request.getAmount());
        accountService.transfer(fromAccountId, staffReq);

        auditEventService.record(
                AuditCategory.PORTAL,
                AuditAction.PORTAL_TRANSFER,
                "ACCOUNT",
                String.valueOf(fromAccountId),
                "Portal transfer executed amount=" + request.getAmount(),
                "toAccountId=" + dest.toAccountId() + ", to=" + dest.accountNumber(),
                true
        );

        return new TransferOutcomeResponse(
                "EXECUTED",
                null,
                TransferRequestStatus.EXECUTED,
                fromAccountId,
                dest.toAccountId(),
                request.getAmount(),
                BeneficiaryBankType.SAME_BANK,
                "Transfer completed",
                java.time.LocalDateTime.now()
        );
    }

    private boolean needsApproval(Customer customer, BeneficiaryBankType bankType, BigDecimal amount) {
        if (bankType == BeneficiaryBankType.OTHER_BANK) {
            return true;
        }
        BigDecimal threshold = customer.getTransferApprovalThreshold();
        return threshold != null
                && threshold.compareTo(BigDecimal.ZERO) > 0
                && amount.compareTo(threshold) >= 0;
    }

    private String approvalReason(Customer customer, BeneficiaryBankType bankType, BigDecimal amount) {
        if (bankType == BeneficiaryBankType.OTHER_BANK) {
            return "Other-bank transfer requires employee approval";
        }
        return "Amount " + amount + " meets/exceeds customer threshold "
                + customer.getTransferApprovalThreshold();
    }

    private Destination fromBeneficiary(Long customerId, Long beneficiaryId) {
        Beneficiary b = beneficiaryRepository.findByBeneficiaryIdAndCustomerId(beneficiaryId, customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Beneficiary not found"));
        if (!Boolean.TRUE.equals(b.getActive())) {
            throw new BadRequestException("Beneficiary is inactive");
        }
        return new Destination(
                b.getBankType(),
                b.getLinkedAccountId(),
                b.getBeneficiaryId(),
                b.getAccountNumber(),
                b.getAccountHolderName(),
                b.getIfsc(),
                b.getBankName()
        );
    }

    private Destination fromAccountNumber(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new BadRequestException("BankOne account number not found"));
        Long customerId = portalCustomerContext.requireCustomerId();
        if (account.getCustomer() != null
                && customerId.equals(account.getCustomer().getCustomerId())) {
            throw new BadRequestException("Cannot transfer to your own account via quick transfer — use internal move later");
        }
        return new Destination(
                BeneficiaryBankType.SAME_BANK,
                account.getAccountId(),
                null,
                account.getAccountNumber(),
                account.getCustomer() == null
                        ? "Account holder"
                        : account.getCustomer().getFirstName() + " " + account.getCustomer().getLastName(),
                null,
                "BankOne"
        );
    }

    private record Destination(
            BeneficiaryBankType bankType,
            Long toAccountId,
            Long beneficiaryId,
            String accountNumber,
            String holderName,
            String ifsc,
            String bankName
    ) {
    }
}
