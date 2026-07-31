package com.bankone.portal.controller;

import com.bankone.account.dto.AccountResponse;
import com.bankone.beneficiary.dto.BeneficiaryResponse;
import com.bankone.beneficiary.dto.CreateBeneficiaryRequest;
import com.bankone.beneficiary.service.BeneficiaryService;
import com.bankone.common.util.PageRequests;
import com.bankone.portal.dto.PortalTransferRequest;
import com.bankone.portal.service.PortalAccountService;
import com.bankone.portal.service.PortalTransferService;
import com.bankone.transaction.dto.TransactionResponse;
import com.bankone.transfer.dto.TransferOutcomeResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/portal")
@PreAuthorize("hasAuthority('ACCESS_PORTAL_ACCOUNTS')")
public class PortalController {

    private static final Set<String> TX_SORT_FIELDS = Set.of("createdAt", "amount", "transactionId");

    private final PortalAccountService portalAccountService;
    private final PortalTransferService portalTransferService;
    private final BeneficiaryService beneficiaryService;

    public PortalController(
            PortalAccountService portalAccountService,
            PortalTransferService portalTransferService,
            BeneficiaryService beneficiaryService
    ) {
        this.portalAccountService = portalAccountService;
        this.portalTransferService = portalTransferService;
        this.beneficiaryService = beneficiaryService;
    }

    @GetMapping("/accounts")
    public ResponseEntity<List<AccountResponse>> myAccounts() {
        return ResponseEntity.ok(portalAccountService.listMyAccounts());
    }

    @GetMapping("/accounts/{accountId}")
    public ResponseEntity<AccountResponse> myAccount(@PathVariable Long accountId) {
        return ResponseEntity.ok(portalAccountService.getMyAccount(accountId));
    }

    @GetMapping("/accounts/{accountId}/transactions")
    public ResponseEntity<Page<TransactionResponse>> myTransactions(
            @PathVariable Long accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Pageable pageable = PageRequests.of(
                page, size, sortBy, sortDir, TX_SORT_FIELDS, "createdAt");
        return ResponseEntity.ok(portalAccountService.getMyTransactions(accountId, pageable));
    }

    @PostMapping("/accounts/{accountId}/transfer")
    public ResponseEntity<TransferOutcomeResponse> transfer(
            @PathVariable Long accountId,
            @Valid @RequestBody PortalTransferRequest request
    ) {
        return ResponseEntity.ok(portalTransferService.transfer(accountId, request));
    }

    @GetMapping("/beneficiaries")
    public ResponseEntity<List<BeneficiaryResponse>> beneficiaries() {
        return ResponseEntity.ok(beneficiaryService.listMine());
    }

    @PostMapping("/beneficiaries")
    public ResponseEntity<BeneficiaryResponse> createBeneficiary(
            @Valid @RequestBody CreateBeneficiaryRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(beneficiaryService.createMine(request));
    }

    @DeleteMapping("/beneficiaries/{id}")
    public ResponseEntity<Void> deleteBeneficiary(@PathVariable Long id) {
        beneficiaryService.deactivateMine(id);
        return ResponseEntity.noContent().build();
    }
}
