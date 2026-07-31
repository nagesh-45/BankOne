package com.bankone.role.service;

import com.bankone.role.AppAccess;
import com.bankone.role.entity.Role;
import com.bankone.role.repository.RoleRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

@Component
public class RoleInitializer {

    private final RoleRepository roleRepository;

    public RoleInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @PostConstruct
    @Transactional
    public void initializeRoles() {
        ensureRole("ADMIN", "System Administrator");
        ensureRole("MANAGER", "Branch Manager");
        ensureRole("EMPLOYEE", "Bank Employee with normal access");
        ensureRole("TELLER", "Bank Teller");
        ensureRole("AUDITOR", "System Auditor");
        ensureRole("CUSTOMER", "Bank Customer");
    }

    private void ensureRole(String roleName, String description) {
        Role role = roleRepository.findByRoleName(roleName).orElseGet(() -> {
            Role created = new Role();
            created.setRoleName(roleName);
            created.setDescription(description);
            return created;
        });

        if (role.getDescription() == null || role.getDescription().isBlank()) {
            role.setDescription(description);
        }

        if (role.getAccessCodes() == null || role.getAccessCodes().isEmpty()) {
            role.setAccessCodes(new HashSet<>(AppAccess.defaultsForRole(roleName)));
        }

        // Keep bank-customer role on portal-only access (never staff dashboard).
        if ("CUSTOMER".equals(roleName)) {
            role.setAccessCodes(new HashSet<>(AppAccess.defaultsForRole("CUSTOMER")));
        }

        // Policy edit: Admin + Manager only. Other staff view via ACCOUNTS_READ.
        if ("ADMIN".equals(roleName) || "MANAGER".equals(roleName)) {
            role.getAccessCodes().add(AppAccess.POLICIES_MANAGE);
        } else if (!"CUSTOMER".equals(roleName)) {
            role.getAccessCodes().remove(AppAccess.POLICIES_MANAGE);
        }

        roleRepository.save(role);
    }
}
