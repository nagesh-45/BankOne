package com.bankone.account.controller;

import com.bankone.account.dto.AccountResponse;
import com.bankone.account.dto.DepositRequest;
import com.bankone.account.dto.OpenAccountRequest;
import com.bankone.account.dto.UpdateAccountStatusRequest;
import com.bankone.account.service.AccountService;
import com.bankone.common.util.PageRequests;
import com.bankone.transaction.dto.TransactionResponse;
import com.bankone.transaction.service.TransactionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import com.bankone.account.dto.WithdrawRequest;
import com.bankone.account.dto.TransferRequest;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private static final Set<String> SORT_FIELDS = Set.of(
            "accountNumber", "accountType", "branchCode", "currencyCode",
            "availableBalance", "status", "createdAt", "accountId"
    );

    private static final Set<String> TX_SORT_FIELDS = Set.of(
            "createdAt", "amount", "transactionId", "transactionType"
    );

    private final AccountService accountService;
    private final TransactionService transactionService;

    public AccountController(AccountService accountService,
                             TransactionService transactionService) {
        this.accountService = accountService;
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<AccountResponse> openAccount(@RequestBody OpenAccountRequest request) {
        AccountResponse response = accountService.openAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<Page<AccountResponse>> getAccountsByCustomer(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Pageable pageable = PageRequests.of(
                page, size, sortBy, sortDir, SORT_FIELDS, "createdAt");
        return ResponseEntity.ok(accountService.getAccountsByCustomerId(customerId, pageable));
    }

    @PutMapping("/{accountId}/status")
    public ResponseEntity<AccountResponse> updateAccountStatus(
            @PathVariable Long accountId,
            @RequestBody UpdateAccountStatusRequest request
    ) {
        return ResponseEntity.ok(accountService.updateAccountStatus(accountId, request));
    }

    @GetMapping
    public ResponseEntity<Page<AccountResponse>> searchAccounts(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Pageable pageable = PageRequests.of(
                page, size, sortBy, sortDir, SORT_FIELDS, "createdAt");
        return ResponseEntity.ok(accountService.searchAccounts(search, pageable));
    }

    @GetMapping("/{accountId}/transactions")
    public ResponseEntity<Page<TransactionResponse>> getTransactions(
            @PathVariable Long accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Pageable pageable = PageRequests.of(
                page, size, sortBy, sortDir, TX_SORT_FIELDS, "createdAt");
        return ResponseEntity.ok(transactionService.getByAccountId(accountId, pageable));
    }

    @PostMapping("/{accountId}/deposit")
    public ResponseEntity<AccountResponse> deposit(
            @PathVariable Long accountId,
            @RequestBody DepositRequest request
    ) {
        return ResponseEntity.ok(accountService.deposit(accountId, request));
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<AccountResponse> getAccountById(@PathVariable Long accountId) {
        return ResponseEntity.ok(accountService.getAccountById(accountId));
    }
    @PostMapping("/{accountId}/withdraw")
    public ResponseEntity<AccountResponse> withdraw(
            @PathVariable Long accountId,
            @RequestBody WithdrawRequest request
    ) {
        return ResponseEntity.ok(accountService.withdraw(accountId, request));
    }
    @PostMapping("/{accountId}/transfer")
    public ResponseEntity<AccountResponse> transfer(
            @PathVariable Long accountId,
            @RequestBody TransferRequest request
    ) {
        return ResponseEntity.ok(accountService.transfer(accountId, request));
    }
}
