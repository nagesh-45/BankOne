package com.bankone.audit.controller;

import com.bankone.audit.domain.AuditCategory;
import com.bankone.audit.dto.AuditBackfillResult;
import com.bankone.audit.dto.AuditEventResponse;
import com.bankone.audit.service.AuditBackfillService;
import com.bankone.audit.service.AuditEventService;
import com.bankone.transfer.dto.PendingTransferResponse;
import com.bankone.transfer.service.TransferApprovalService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/audit")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'AUDITOR')")
public class AuditController {

    private final TransferApprovalService transferApprovalService;
    private final AuditEventService auditEventService;
    private final AuditBackfillService auditBackfillService;

    public AuditController(
            TransferApprovalService transferApprovalService,
            AuditEventService auditEventService,
            AuditBackfillService auditBackfillService
    ) {
        this.transferApprovalService = transferApprovalService;
        this.auditEventService = auditEventService;
        this.auditBackfillService = auditBackfillService;
    }

    /**
     * Activity trail across categories (AUTH, CUSTOMER, ACCOUNT, …).
     * Filters: category, action, actor (username contains).
     */
    @GetMapping("/events")
    public ResponseEntity<Page<AuditEventResponse>> events(
            @RequestParam(required = false) AuditCategory category,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String actor,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size
    ) {
        return ResponseEntity.ok(auditEventService.search(category, action, actor, page, size));
    }

    @GetMapping("/transfer-approvals")
    public ResponseEntity<List<PendingTransferResponse>> transferApprovalHistory() {
        return ResponseEntity.ok(transferApprovalService.listAuditHistory());
    }

    /** Rebuild missing historical events from existing tables (safe to run repeatedly). */
    @PostMapping("/backfill")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuditBackfillResult> backfill() {
        return ResponseEntity.ok(auditBackfillService.backfill());
    }
}
