package com.bankone.account.controller;

import com.bankone.account.dto.AccountPolicyResponse;
import com.bankone.account.dto.CreateAccountPolicyRequest;
import com.bankone.account.service.AccountPolicyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/account-policies")
public class AccountPolicyController {

    private final AccountPolicyService accountPolicyService;

    public AccountPolicyController(AccountPolicyService accountPolicyService) {
        this.accountPolicyService = accountPolicyService;
    }

    @PostMapping
    public ResponseEntity<AccountPolicyResponse> createPolicy(
            @RequestBody CreateAccountPolicyRequest request) {

        AccountPolicyResponse response = accountPolicyService.createPolicy(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}