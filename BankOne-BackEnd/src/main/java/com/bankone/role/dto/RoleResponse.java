package com.bankone.role.dto;

import java.util.List;

public record RoleResponse(
        Long roleId,
        String roleName,
        String description,
        List<String> accessCodes,
        boolean systemRole
) {
}
