package com.bankone.role.service;

import com.bankone.role.entity.Role;
import com.bankone.role.repository.RoleRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class RoleInitializer {

    private final RoleRepository roleRepository;

    public RoleInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @PostConstruct
    public void initializeRoles() {
        createRole("ADMIN", "System Administrator");
        createRole("MANAGER", "Branch Manager");
        createRole("TELLER", "Bank Teller");
        createRole("AUDITOR", "System Auditor");
        createRole("CUSTOMER", "Bank Customer");
    }

    private void createRole(String roleName, String description) {
        if (roleRepository.findByRoleName(roleName).isEmpty()) {
            Role role = new Role();
            role.setRoleName(roleName);
            role.setDescription(description);
            roleRepository.save(role);
        }
    }
}