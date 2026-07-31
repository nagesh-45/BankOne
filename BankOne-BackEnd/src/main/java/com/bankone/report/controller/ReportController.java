package com.bankone.report.controller;

import com.bankone.report.dto.AccountMixReport;
import com.bankone.report.dto.ApprovalsReport;
import com.bankone.report.dto.TransactionTrendsReport;
import com.bankone.report.service.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/transaction-trends")
    @PreAuthorize("hasAuthority('ACCESS_ACCOUNTS_READ')")
    public ResponseEntity<TransactionTrendsReport> transactionTrends(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(reportService.transactionTrends(from, to));
    }

    @GetMapping("/transaction-trends/pdf")
    @PreAuthorize("hasAuthority('ACCESS_ACCOUNTS_READ')")
    public ResponseEntity<byte[]> transactionTrendsPdf(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return pdfResponse(
                "transaction-trends.pdf",
                reportService.transactionTrendsPdf(from, to)
        );
    }

    @GetMapping("/account-mix")
    @PreAuthorize("hasAuthority('ACCESS_ACCOUNTS_READ')")
    public ResponseEntity<AccountMixReport> accountMix() {
        return ResponseEntity.ok(reportService.accountMix());
    }

    @GetMapping("/account-mix/pdf")
    @PreAuthorize("hasAuthority('ACCESS_ACCOUNTS_READ')")
    public ResponseEntity<byte[]> accountMixPdf() {
        return pdfResponse("account-mix.pdf", reportService.accountMixPdf());
    }

    @GetMapping("/approvals")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','AUDITOR')")
    public ResponseEntity<ApprovalsReport> approvals(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(reportService.approvals(from, to));
    }

    @GetMapping("/approvals/pdf")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','AUDITOR')")
    public ResponseEntity<byte[]> approvalsPdf(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return pdfResponse("approvals.pdf", reportService.approvalsPdf(from, to));
    }

    private ResponseEntity<byte[]> pdfResponse(String filename, byte[] body) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(body);
    }
}
