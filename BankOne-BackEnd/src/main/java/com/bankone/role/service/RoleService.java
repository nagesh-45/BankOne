package com.bankone.role.service;

import com.bankone.audit.domain.AuditAction;
import com.bankone.audit.domain.AuditCategory;
import com.bankone.audit.service.AuditEventService;
import com.bankone.cache.CacheNames;
import com.bankone.common.exception.BadRequestException;
import com.bankone.common.exception.ConflictException;
import com.bankone.common.exception.ResourceNotFoundException;
import com.bankone.role.AppAccess;
import com.bankone.role.dto.CreateRoleRequest;
import com.bankone.role.dto.RoleResponse;
import com.bankone.role.dto.UpdateRoleRequest;
import com.bankone.role.entity.Role;
import com.bankone.role.repository.RoleRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoleService {

    private static final Set<String> SYSTEM_ROLES = Set.of(
            "ADMIN", "MANAGER", "EMPLOYEE", "TELLER", "AUDITOR", "CUSTOMER"
    );

    private final RoleRepository roleRepository;
    private final AuditEventService auditEventService;

    public RoleService(RoleRepository roleRepository, AuditEventService auditEventService) {
        this.roleRepository = roleRepository;
        this.auditEventService = auditEventService;
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheNames.ROLES, key = "'all'")
    public List<RoleResponse> listRoles() {
        return roleRepository.findAll().stream()
                .sorted(Comparator.comparing(Role::getRoleName))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheNames.ROLES, key = "'id:' + #roleId")
    public RoleResponse getRole(Long roleId) {
        return toResponse(findRole(roleId));
    }

    @Transactional
    @CacheEvict(cacheNames = CacheNames.ROLES, allEntries = true)
    public RoleResponse createRole(CreateRoleRequest request) {
        String name = normalizeRoleName(request.getRoleName());
        if (roleRepository.findByRoleName(name).isPresent()) {
            throw new ConflictException("Role already exists: " + name);
        }

        Role role = new Role();
        role.setRoleName(name);
        role.setDescription(trimToNull(request.getDescription()));
        role.setAccessCodes(validateAccessCodes(request.getAccessCodes()));
        RoleResponse response = toResponse(roleRepository.save(role));
        auditEventService.record(
                AuditCategory.ROLE,
                AuditAction.ROLE_CREATE,
                "ROLE",
                String.valueOf(response.roleId()),
                "Role created: " + name,
                "accesses=" + String.join(",", response.accessCodes()),
                true
        );
        return response;
    }

    @Transactional
    @CacheEvict(cacheNames = CacheNames.ROLES, allEntries = true)
    public RoleResponse updateRole(Long roleId, UpdateRoleRequest request) {
        Role role = findRole(roleId);

        if (request.getDescription() != null) {
            role.setDescription(trimToNull(request.getDescription()));
        }

        if (request.getAccessCodes() != null) {
            Set<String> codes = validateAccessCodes(request.getAccessCodes());
            if ("ADMIN".equals(role.getRoleName())) {
                codes.add(AppAccess.USERS_MANAGE);
                codes.add(AppAccess.ROLES_MANAGE);
                codes.remove(AppAccess.PORTAL_ACCOUNTS);
            }
            if ("CUSTOMER".equals(role.getRoleName())) {
                codes = new java.util.HashSet<>(Set.of(AppAccess.PORTAL_ACCOUNTS));
            }
            role.setAccessCodes(codes);
        }

        RoleResponse response = toResponse(roleRepository.save(role));
        auditEventService.record(
                AuditCategory.ROLE,
                AuditAction.ROLE_UPDATE,
                "ROLE",
                String.valueOf(response.roleId()),
                "Role updated: " + response.roleName(),
                "accesses=" + String.join(",", response.accessCodes()),
                true
        );
        return response;
    }

    private Role findRole(Long roleId) {
        return roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleId));
    }

    private Set<String> validateAccessCodes(List<String> raw) {
        if (raw == null || raw.isEmpty()) {
            return new HashSet<>();
        }
        Set<String> codes = new LinkedHashSet<>();
        for (String item : raw) {
            String code = AppAccess.normalize(item);
            if (!AppAccess.isKnown(code)) {
                throw new BadRequestException("Unknown access code: " + item);
            }
            codes.add(code);
        }
        return codes;
    }

    private String normalizeRoleName(String roleName) {
        if (roleName == null || roleName.isBlank()) {
            throw new BadRequestException("Role name is required");
        }
        String name = roleName.trim().toUpperCase(Locale.ROOT).replace(' ', '_');
        if (!name.matches("[A-Z][A-Z0-9_]{1,49}")) {
            throw new BadRequestException(
                    "Role name must be 2–50 chars: letters, numbers, underscore (e.g. BRANCH_LEAD)");
        }
        return name;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private RoleResponse toResponse(Role role) {
        List<String> codes = role.getAccessCodes() == null
                ? List.of()
                : role.getAccessCodes().stream().sorted().collect(Collectors.toList());
        return new RoleResponse(
                role.getRoleId(),
                role.getRoleName(),
                role.getDescription(),
                codes,
                SYSTEM_ROLES.contains(role.getRoleName())
        );
    }
}
