package com.bankone.account.dto;

import com.bankone.account.enums.AccountStatus;

public class UpdateAccountStatusRequest {

    private AccountStatus status;

    public UpdateAccountStatusRequest() {
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }
}
