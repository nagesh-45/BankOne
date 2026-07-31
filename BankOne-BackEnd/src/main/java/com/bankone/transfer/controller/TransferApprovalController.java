package com.bankone.transfer.controller;

import com.bankone.transfer.dto.PendingTransferResponse;
import com.bankone.transfer.dto.ResolveTransferRequest;
import com.bankone.transfer.service.TransferApprovalService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/transfer-approvals")
public class TransferApprovalController {

    private final TransferApprovalService transferApprovalService;

    public TransferApprovalController(TransferApprovalService transferApprovalService) {
        this.transferApprovalService = transferApprovalService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ACCESS_ACCOUNTS_WRITE')")
    public ResponseEntity<List<PendingTransferResponse>> listPending() {
        return ResponseEntity.ok(transferApprovalService.listPending());
    }

    @GetMapping("/my-history")
    @PreAuthorize("hasAuthority('ACCESS_ACCOUNTS_WRITE')")
    public ResponseEntity<List<PendingTransferResponse>> myHistory() {
        return ResponseEntity.ok(transferApprovalService.listMyHistory());
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('ACCESS_ACCOUNTS_WRITE')")
    public ResponseEntity<PendingTransferResponse> approve(@PathVariable Long id) {
        return ResponseEntity.ok(transferApprovalService.approve(id));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('ACCESS_ACCOUNTS_WRITE')")
    public ResponseEntity<PendingTransferResponse> reject(
            @PathVariable Long id,
            @RequestBody(required = false) ResolveTransferRequest request
    ) {
        return ResponseEntity.ok(transferApprovalService.reject(id, request));
    }
}
