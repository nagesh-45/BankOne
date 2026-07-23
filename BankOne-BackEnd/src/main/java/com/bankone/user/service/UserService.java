package com.bankone.user.service;

import com.bankone.common.exception.BadRequestException;
import com.bankone.common.exception.ConflictException;
import com.bankone.common.exception.ResourceNotFoundException;
import com.bankone.common.util.BusinessIdFormatter;
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
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class UserService {

    private static final Set<String> EMPLOYEE_ROLES = Set.of("ADMIN", "EMPLOYEE", "MANAGER");

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            UserRoleRepository userRoleRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (request.getUserType() == CreateUserRequest.UserType.CUSTOMER) {
            throw new BadRequestException(
                    "Bank customer user creation is not available yet. Please create an employee for now.");
        }

        if (request.getAccessLevel() == null) {
            throw new BadRequestException("Access level is required for employees");
        }

        if (userRepository.existsByUsername(request.getUsername().trim())) {
            throw new ConflictException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail().trim())) {
            throw new ConflictException("Email already exists");
        }

        String roleName = toRoleName(request.getAccessLevel());
        Role role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new BadRequestException("Role not found: " + roleName));

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

        User savedUser = userRepository.save(user);

        UserRole userRole = new UserRole();
        userRole.setUser(savedUser);
        userRole.setRole(role);
        userRole.setRoleName(role.getRoleName());
        userRole.setActive(true);
        userRoleRepository.save(userRole);

        return toResponse(savedUser, List.of(role.getRoleName()));
    }

    @Transactional
    public UserResponse updateUser(Long userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + userId));

        List<UserRole> existingRoles = userRoleRepository.findByUserWithRole(user);
        boolean isEmployee = existingRoles.stream()
                .anyMatch(role -> Boolean.TRUE.equals(role.getActive())
                        && EMPLOYEE_ROLES.contains(role.getRoleName()));

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

        String targetRoleName = toRoleName(request.getAccessLevel());
        Role targetRole = roleRepository.findByRoleName(targetRoleName)
                .orElseThrow(() -> new BadRequestException("Role not found: " + targetRoleName));

        boolean alreadyHasTarget = false;
        for (UserRole userRole : existingRoles) {
            if (!Boolean.TRUE.equals(userRole.getActive())) {
                continue;
            }
            if (targetRoleName.equals(userRole.getRoleName())) {
                alreadyHasTarget = true;
            } else if (EMPLOYEE_ROLES.contains(userRole.getRoleName())) {
                userRole.setActive(false);
                userRoleRepository.save(userRole);
            }
        }

        if (!alreadyHasTarget) {
            UserRole userRole = new UserRole();
            userRole.setUser(savedUser);
            userRole.setRole(targetRole);
            userRole.setRoleName(targetRole.getRoleName());
            userRole.setActive(true);
            userRoleRepository.save(userRole);
        }

        return toResponse(savedUser, List.of(targetRoleName));
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

    private String toRoleName(CreateUserRequest.AccessLevel accessLevel) {
        return accessLevel == CreateUserRequest.AccessLevel.ADMIN ? "ADMIN" : "EMPLOYEE";
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
