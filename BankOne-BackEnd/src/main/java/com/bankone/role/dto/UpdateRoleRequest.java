package com.bankone.role.dto;

import jakarta.validation.constraints.Size;

import java.util.List;

public class UpdateRoleRequest {

    @Size(max = 255)
    private String description;

    private List<String> accessCodes;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getAccessCodes() {
        return accessCodes;
    }

    public void setAccessCodes(List<String> accessCodes) {
        this.accessCodes = accessCodes;
    }
}
