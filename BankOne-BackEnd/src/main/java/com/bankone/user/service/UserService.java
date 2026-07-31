package com.bankone.user.service;

import com.bankone.audit.domain.AuditAction;
import com.bankone.audit.domain.AuditCategory;
import com.bankone.audit.service.AuditEventService;
import com.bankone.common.exception.BadRequestException;
import com.bankone.common.exception.ConflictException;
import com.bankone.common.exception.ResourceNotFoundException;
import com.bankone.common.util.BusinessIdFormatter;
import com.bankone.customer.repository.CustomerRepository;
import com.bankone.role.entity.Role;
import com.bankone.role.repository.RoleRepository;
import com.bankone.user.dto.CreateUserRequest;
import com.bankone.user.dto.UpdateUserRequest;
import com.bankone.user.dto.UserResponse;
import com.bankone.user.entity.User;
import com.bankone.user.entity.UserRole;
import com.bankone.user.repository.UserRepository;
import com.bankone.user.repository.UserRoleRepository;
import com.bankone.user.specification.EmployeeSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    /** Roles that identify a staff account (vs portal customer). */
    private static final Set<String> STAFF_ROLES = Set.of(
            "ADMIN", "EMPLOYEE", "MANAGER", "TELLER", "AUDITOR"
    );

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditEventService auditEventService;

    public UserService(
            UserRepository userRepository,
            UserRoleRepository userRoleRepository,
            RoleRepository roleRepository,
            CustomerRepository customerRepository,
            PasswordEncoder passwordEncoder,
            AuditEventService auditEventService
    ) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditEventService = auditEventService;
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (request.getUserType() == CreateUserRequest.UserType.CUSTOMER) {
            return createPortalCustomerUser(request);
        }

        List<Role> roles = resolveStaffRoles(request.getRoleNames(), request.getRoleName(), request.getAccessLevel());

        if (userRepository.existsByUsername(request.getUsername().trim())) {
            throw new ConflictException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail().trim())) {
            throw new ConflictException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setEmail(request.getEmail().trim());
        user.setEnabled(true);
        user.setAccountLocked(false);
        user.setCredentialsExpired(false);
        user.setFailedLoginAttempts(0);
        user.setCustomerId(null);

        User savedUser = userRepository.save(user);

        List<String> assigned = new ArrayList<>();
        for (Role role : roles) {
            UserRole userRole = new UserRole();
            userRole.setUser(savedUser);
            userRole.setRole(role);
            userRole.setRoleName(role.getRoleName());
            userRole.setActive(true);
            userRoleRepository.save(userRole);
            assigned.add(role.getRoleName());
        }

        UserResponse response = toResponse(savedUser, assigned);
        auditEventService.record(
                AuditCategory.STAFF,
                AuditAction.USER_CREATE,
                "USER",
                String.valueOf(savedUser.getUserId()),
                "Staff user created: " + savedUser.getUsername(),
                "roles=" + String.join(",", assigned),
                true
        );
        return response;
    }

    private UserResponse createPortalCustomerUser(CreateUserRequest request) {
        if (request.getCustomerId() == null) {
            throw new BadRequestException("customerId is required to create a portal login");
        }

        Long customerId = request.getCustomerId();
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer not found: " + customerId);
        }
        if (userRepository.existsByCustomerId(customerId)) {
            throw new ConflictException("This customer already has a portal login");
        }

        Role role = roleRepository.findByRoleName("CUSTOMER")
                .orElseThrow(() -> new BadRequestException("Role not found: CUSTOMER"));

        if (userRepository.existsByUsername(request.getUsername().trim())) {
            throw new ConflictException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail().trim())) {
            throw new ConflictException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setEmail(request.getEmail().trim());
        user.setEnabled(true);
        user.setAccountLocked(false);
        user.setCredentialsExpired(false);
        user.setFailedLoginAttempts(0);
        user.setCustomerId(customerId);

        User savedUser = userRepository.save(user);

        UserRole userRole = new UserRole();
        userRole.setUser(savedUser);
        userRole.setRole(role);
        userRole.setRoleName(role.getRoleName());
        userRole.setActive(true);
        userRoleRepository.save(userRole);

        UserResponse response = toResponse(savedUser, List.of("CUSTOMER"));
        auditEventService.record(
                AuditCategory.PORTAL,
                AuditAction.USER_CREATE,
                "USER",
                String.valueOf(savedUser.getUserId()),
                "Portal login created: " + savedUser.getUsername(),
                "customerId=" + customerId,
                true
        );
        return response;
    }

    @Transactional
    public UserResponse updateUser(Long userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + userId));

        List<UserRole> existingRoles = userRoleRepository.findByUserWithRole(user);
        boolean isEmployee = existingRoles.stream()
                .anyMatch(role -> Boolean.TRUE.equals(role.getActive())
                        && !"CUSTOMER".equals(role.getRoleName()));

        if (!isEmployee) {
            throw new BadRequestException("Only employee accounts can be updated here");
        }

        String email = request.getEmail().trim();
        if (userRepository.existsByEmailIgnoreCaseAndUserIdNot(email, userId)) {
            throw new ConflictException("Email already exists");
        }

        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setEmail(email);
        user.setEnabled(Boolean.TRUE.equals(request.getEnabled()));

        User savedUser = userRepository.save(user);

        List<Role> targetRoles = resolveStaffRoles(
                request.getRoleNames(),
                request.getRoleName(),
                request.getAccessLevel()
        );
        Set<String> targetNames = targetRoles.stream()
                .map(Role::getRoleName)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Map<String, UserRole> byRoleName = new HashMap<>();
        for (UserRole userRole : existingRoles) {
            byRoleName.put(userRole.getRoleName(), userRole);
        }

        for (UserRole userRole : existingRoles) {
            if (!Boolean.TRUE.equals(userRole.getActive())) {
                continue;
            }
            if (STAFF_ROLES.contains(userRole.getRoleName())
                    || userRole.getRole() != null) {
                if (!targetNames.contains(userRole.getRoleName())
                        && !"CUSTOMER".equals(userRole.getRoleName())) {
                    userRole.setActive(false);
                    userRoleRepository.save(userRole);
                }
            }
        }

        List<String> assigned = new ArrayList<>();
        for (Role role : targetRoles) {
            UserRole existing = byRoleName.get(role.getRoleName());
            if (existing != null) {
                existing.setActive(true);
                existing.setRole(role);
                existing.setRoleName(role.getRoleName());
                userRoleRepository.save(existing);
            } else {
                UserRole userRole = new UserRole();
                userRole.setUser(savedUser);
                userRole.setRole(role);
                userRole.setRoleName(role.getRoleName());
                userRole.setActive(true);
                userRoleRepository.save(userRole);
            }
            assigned.add(role.getRoleName());
        }

        UserResponse response = toResponse(savedUser, assigned);
        auditEventService.record(
                AuditCategory.STAFF,
                AuditAction.USER_UPDATE,
                "USER",
                String.valueOf(savedUser.getUserId()),
                "Staff user updated: " + savedUser.getUsername(),
                "roles=" + String.join(",", assigned) + ", enabled=" + savedUser.getEnabled(),
                true
        );
        return response;
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> getEmployees(String search, Pageable pageable) {
        Page<User> users = userRepository.findAll(
                EmployeeSpecification.matching(search),
                pageable
        );

        List<Long> userIds = users.getContent().stream()
                .map(User::getUserId)
                .toList();

        Map<Long, List<String>> rolesByUserId = new HashMap<>();
        if (!userIds.isEmpty()) {
            for (UserRole userRole : userRoleRepository.findActiveByUserIds(userIds)) {
                rolesByUserId
                        .computeIfAbsent(userRole.getUser().getUserId(), ignored -> new ArrayList<>())
                        .add(userRole.getRole().getRoleName());
            }
        }

        return users.map(user -> toResponse(
                user,
                rolesByUserId.getOrDefault(user.getUserId(), List.of())
        ));
    }

    private List<Role> resolveStaffRoles(
            List<String> roleNames,
            String roleName,
            CreateUserRequest.AccessLevel accessLevel
    ) {
        LinkedHashSet<String> names = new LinkedHashSet<>();
        if (roleNames != null) {
            for (String name : roleNames) {
                if (name != null && !name.isBlank()) {
                    names.add(normalizeStaffRoleName(name));
                }
            }
        }
        if (names.isEmpty() && roleName != null && !roleName.isBlank()) {
            names.add(normalizeStaffRoleName(roleName));
        }
        if (names.isEmpty()) {
            if (accessLevel == null) {
                throw new BadRequestException("At least one role is required for employees");
            }
            names.add(accessLevel == CreateUserRequest.AccessLevel.ADMIN ? "ADMIN" : "EMPLOYEE");
        }

        List<Role> roles = new ArrayList<>();
        for (String name : names) {
            Role role = roleRepository.findByRoleName(name)
                    .orElseThrow(() -> new BadRequestException("Role not found: " + name));
            if ("CUSTOMER".equals(role.getRoleName())) {
                throw new BadRequestException("CUSTOMER role cannot be assigned to staff users");
            }
            roles.add(role);
        }
        return roles;
    }

    private String normalizeStaffRoleName(String roleName) {
        String normalized = roleName.trim().toUpperCase(Locale.ROOT);
        if ("CUSTOMER".equals(normalized)) {
            throw new BadRequestException("CUSTOMER role cannot be assigned to staff users");
        }
        if (!STAFF_ROLES.contains(normalized)) {
            Role custom = roleRepository.findByRoleName(normalized)
                    .orElseThrow(() -> new BadRequestException("Role not found: " + normalized));
            if ("CUSTOMER".equals(custom.getRoleName())) {
                throw new BadRequestException("CUSTOMER role cannot be assigned to staff users");
            }
            return custom.getRoleName();
        }
        return normalized;
    }

    private UserResponse toResponse(User user, List<String> roles) {
        return new UserResponse(
                user.getUserId(),
                BusinessIdFormatter.employeeCode(user.getUserId()),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getEnabled(),
                roles
        );
    }
}
