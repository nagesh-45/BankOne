package com.bankone.account.controller;

import com.bankone.account.dto.AccountPolicyResponse;
import com.bankone.account.dto.CreateAccountPolicyRequest;
import com.bankone.account.dto.UpdateAccountPolicyRequest;
import com.bankone.account.service.AccountPolicyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/account-policies")
public class AccountPolicyController {

    private final AccountPolicyService accountPolicyService;

    public AccountPolicyController(AccountPolicyService accountPolicyService) {
        this.accountPolicyService = accountPolicyService;
    }

    /** Staff list — Admin/Manager edit elsewhere; all staff with accounts/policy access can view. */
    @GetMapping("/all")
    @PreAuthorize(
            "hasAnyAuthority('ACCESS_POLICIES_MANAGE','ACCESS_ACCOUNTS_READ','ACCESS_ACCOUNTS_WRITE')"
    )
    public ResponseEntity<List<AccountPolicyResponse>> listAll() {
        return ResponseEntity.ok(accountPolicyService.listAll());
    }

    @GetMapping
    @PreAuthorize(
            "hasAnyAuthority('ACCESS_POLICIES_MANAGE','ACCESS_ACCOUNTS_READ','ACCESS_ACCOUNTS_WRITE')"
    )
    public ResponseEntity<AccountPolicyResponse> getActivePolicy(
            @RequestParam String accountType,
            @RequestParam(defaultValue = "INR") String currencyCode
    ) {
        return ResponseEntity.ok(
                accountPolicyService.getActivePolicy(accountType, currencyCode));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ACCESS_POLICIES_MANAGE') and hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<AccountPolicyResponse> createPolicy(
            @Valid @RequestBody CreateAccountPolicyRequest request
    ) {
        AccountPolicyResponse response = accountPolicyService.createPolicy(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{policyId}")
    @PreAuthorize("hasAuthority('ACCESS_POLICIES_MANAGE') and hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<AccountPolicyResponse> updatePolicy(
            @PathVariable Long policyId,
            @Valid @RequestBody UpdateAccountPolicyRequest request
    ) {
        return ResponseEntity.ok(accountPolicyService.updatePolicy(policyId, request));
    }
}
