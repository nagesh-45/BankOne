package com.bankone.role.dto;

import java.io.Serializable;
import java.util.List;

public record RoleResponse(
        Long roleId,
        String roleName,
        String description,
        List<String> accessCodes,
        boolean systemRole
) implements Serializable {
    private static final long serialVersionUID = 1L;
}
