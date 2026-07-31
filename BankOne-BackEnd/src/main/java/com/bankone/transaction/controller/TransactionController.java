package com.bankone.transaction.controller;

import com.bankone.transaction.dto.TransactionResponse;
import com.bankone.transaction.enums.TransactionType;
import com.bankone.transaction.service.TransactionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;
import java.util.Set;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private static final Set<String> SORT_FIELDS = Set.of(
            "createdAt", "amount", "transactionId", "transactionType"
    );

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ACCESS_ACCOUNTS_READ')")
    public ResponseEntity<Page<TransactionResponse>> listAll(
            @RequestParam(required = false) Long accountId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        int safeSize = Math.min(Math.max(size, 1), 100);
        String safeSortBy = SORT_FIELDS.contains(sortBy) ? sortBy : "createdAt";
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, safeSize, Sort.by(direction, safeSortBy));

        TransactionType txType = null;
        if (StringUtils.hasText(type)) {
            txType = TransactionType.valueOf(type.trim().toUpperCase(Locale.ROOT));
        }

        return ResponseEntity.ok(transactionService.listAll(accountId, txType, search, pageable));
    }
}
