package com.bankone.role.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public class CreateRoleRequest {

    @NotBlank
    @Size(max = 50)
    private String roleName;

    @Size(max = 255)
    private String description;

    private List<String> accessCodes;

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

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
