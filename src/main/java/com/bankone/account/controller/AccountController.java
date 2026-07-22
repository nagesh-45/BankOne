package com.bankone.account.controller;

import com.bankone.account.dto.AccountResponse;
import com.bankone.account.dto.OpenAccountRequest;
import com.bankone.account.dto.UpdateAccountStatusRequest;
import com.bankone.account.service.AccountService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
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
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1));
        return ResponseEntity.ok(accountService.getAccountsByCustomerId(customerId, pageable));
    }

    @PutMapping("/{accountId}/status")
    public ResponseEntity<AccountResponse> updateAccountStatus(
            @PathVariable Long accountId,
            @RequestBody UpdateAccountStatusRequest request
    ) {
        return ResponseEntity.ok(accountService.updateAccountStatus(accountId, request));
    }
}
