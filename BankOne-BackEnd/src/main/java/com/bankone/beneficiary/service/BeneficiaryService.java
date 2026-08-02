package com.bankone.beneficiary.service;

import com.bankone.account.entity.Account;
import com.bankone.account.repository.AccountRepository;
import com.bankone.audit.domain.AuditAction;
import com.bankone.audit.domain.AuditCategory;
import com.bankone.audit.service.AuditEventService;
import com.bankone.beneficiary.dto.BeneficiaryResponse;
import com.bankone.beneficiary.dto.CreateBeneficiaryRequest;
import com.bankone.beneficiary.entity.Beneficiary;
import com.bankone.beneficiary.enums.BeneficiaryBankType;
import com.bankone.beneficiary.repository.BeneficiaryRepository;
import com.bankone.cache.CacheNames;
import com.bankone.common.exception.BadRequestException;
import com.bankone.common.exception.ResourceNotFoundException;
import com.bankone.portal.service.PortalCustomerContext;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class BeneficiaryService {

    private final BeneficiaryRepository beneficiaryRepository;
    private final AccountRepository accountRepository;
    private final PortalCustomerContext portalCustomerContext;
    private final AuditEventService auditEventService;

    public BeneficiaryService(
            BeneficiaryRepository beneficiaryRepository,
            AccountRepository accountRepository,
            PortalCustomerContext portalCustomerContext,
            AuditEventService auditEventService
    ) {
        this.beneficiaryRepository = beneficiaryRepository;
        this.accountRepository = accountRepository;
        this.portalCustomerContext = portalCustomerContext;
        this.auditEventService = auditEventService;
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheNames.BENEFICIARIES, key = "'user:' + @portalCustomerContext.requireCustomerId()")
    public List<BeneficiaryResponse> listMine() {
        Long customerId = portalCustomerContext.requireCustomerId();
        return beneficiaryRepository.findByCustomerIdAndActiveTrueOrderByNicknameAsc(customerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    @CacheEvict(cacheNames = CacheNames.BENEFICIARIES, allEntries = true)
    public BeneficiaryResponse createMine(CreateBeneficiaryRequest request) {
        Long customerId = portalCustomerContext.requireCustomerId();
        BeneficiaryBankType type = request.getBankType();
        if (type == null) {
            throw new BadRequestException("bankType is required (SAME_BANK or OTHER_BANK)");
        }

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setCustomerId(customerId);
        beneficiary.setNickname(request.getNickname().trim());
        beneficiary.setBankType(type);
        beneficiary.setAccountNumber(request.getAccountNumber().trim());
        beneficiary.setAccountHolderName(request.getAccountHolderName().trim());
        beneficiary.setActive(true);

        if (type == BeneficiaryBankType.SAME_BANK) {
            Account account = accountRepository.findByAccountNumber(request.getAccountNumber().trim())
                    .orElseThrow(() -> new BadRequestException(
                            "Same-bank account number not found in BankOne"));
            if (account.getCustomer() != null
                    && customerId.equals(account.getCustomer().getCustomerId())) {
                throw new BadRequestException("Cannot add your own account as a beneficiary");
            }
            beneficiary.setLinkedAccountId(account.getAccountId());
            beneficiary.setIfsc(null);
            beneficiary.setBankName("BankOne");
        } else {
            if (!StringUtils.hasText(request.getIfsc()) || !StringUtils.hasText(request.getBankName())) {
                throw new BadRequestException("IFSC and bank name are required for other-bank beneficiaries");
            }
            beneficiary.setIfsc(request.getIfsc().trim().toUpperCase());
            beneficiary.setBankName(request.getBankName().trim());
            beneficiary.setLinkedAccountId(null);
        }

        BeneficiaryResponse response = toResponse(beneficiaryRepository.save(beneficiary));
        auditEventService.record(
                AuditCategory.PORTAL,
                AuditAction.BENEFICIARY_CREATE,
                "BENEFICIARY",
                String.valueOf(response.beneficiaryId()),
                "Beneficiary added: " + response.nickname(),
                "account=" + response.accountNumber() + ", bankType=" + response.bankType(),
                true
        );
        return response;
    }

    @Transactional
    @CacheEvict(cacheNames = CacheNames.BENEFICIARIES, allEntries = true)
    public void deactivateMine(Long beneficiaryId) {
        Long customerId = portalCustomerContext.requireCustomerId();
        Beneficiary beneficiary = beneficiaryRepository
                .findByBeneficiaryIdAndCustomerId(beneficiaryId, customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Beneficiary not found"));
        beneficiary.setActive(false);
        beneficiaryRepository.save(beneficiary);
        auditEventService.record(
                AuditCategory.PORTAL,
                AuditAction.BENEFICIARY_DELETE,
                "BENEFICIARY",
                String.valueOf(beneficiaryId),
                "Beneficiary deactivated: " + beneficiary.getNickname(),
                null,
                true
        );
    }

    private BeneficiaryResponse toResponse(Beneficiary b) {
        return new BeneficiaryResponse(
                b.getBeneficiaryId(),
                b.getNickname(),
                b.getBankType(),
                b.getAccountNumber(),
                b.getAccountHolderName(),
                b.getLinkedAccountId(),
                b.getIfsc(),
                b.getBankName(),
                b.getCreatedAt()
        );
    }
}
