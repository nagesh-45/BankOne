package com.bankone.transfer.dto;

import jakarta.validation.constraints.Size;

public class ResolveTransferRequest {

    @Size(max = 255)
    private String rejectionReason;

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
}
