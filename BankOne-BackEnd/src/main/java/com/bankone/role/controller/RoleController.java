package com.bankone.role.controller;

import com.bankone.role.AppAccess;
import com.bankone.role.dto.CreateRoleRequest;
import com.bankone.role.dto.RoleResponse;
import com.bankone.role.dto.UpdateRoleRequest;
import com.bankone.role.service.RoleService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping("/access-catalog")
    @PreAuthorize("hasAuthority('ACCESS_ROLES_MANAGE')")
    public ResponseEntity<List<AppAccess.AccessDefinition>> accessCatalog() {
        return ResponseEntity.ok(AppAccess.catalog());
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ACCESS_ROLES_MANAGE') or hasAuthority('ACCESS_USERS_MANAGE')")
    public ResponseEntity<List<RoleResponse>> listRoles() {
        return ResponseEntity.ok(roleService.listRoles());
    }

    @GetMapping("/{id:\\d+}")
    @PreAuthorize("hasAuthority('ACCESS_ROLES_MANAGE')")
    public ResponseEntity<RoleResponse> getRole(@PathVariable Long id) {
        return ResponseEntity.ok(roleService.getRole(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ACCESS_ROLES_MANAGE')")
    public ResponseEntity<RoleResponse> createRole(@Valid @RequestBody CreateRoleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roleService.createRole(request));
    }

    @PutMapping("/{id:\\d+}")
    @PreAuthorize("hasAuthority('ACCESS_ROLES_MANAGE')")
    public ResponseEntity<RoleResponse> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoleRequest request
    ) {
        return ResponseEntity.ok(roleService.updateRole(id, request));
    }
}
