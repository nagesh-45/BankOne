package com.bankone.user.service;

import com.bankone.common.exception.BadRequestException;
import com.bankone.common.exception.ConflictException;
import com.bankone.role.entity.Role;
import com.bankone.role.repository.RoleRepository;
import com.bankone.user.dto.CreateUserRequest;
import com.bankone.user.dto.UserResponse;
import com.bankone.user.entity.User;
import com.bankone.user.entity.UserRole;
import com.bankone.user.repository.UserRepository;
import com.bankone.user.repository.UserRoleRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

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

        String roleName = request.getAccessLevel() == CreateUserRequest.AccessLevel.ADMIN
                ? "ADMIN"
                : "EMPLOYEE";

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

    @Transactional(readOnly = true)
    public List<UserResponse> getEmployees(String search) {
        String term = search == null ? "" : search.trim().toLowerCase();

        return userRepository.findAll().stream()
                .map(user -> {
                    List<String> roles = userRoleRepository.findByUserWithRole(user).stream()
                            .filter(userRole -> Boolean.TRUE.equals(userRole.getActive()))
                            .map(userRole -> userRole.getRole().getRoleName())
                            .toList();
                    return toResponse(user, roles);
                })
                .filter(response -> response.getRoles().stream()
                        .anyMatch(role -> "ADMIN".equals(role) || "EMPLOYEE".equals(role) || "MANAGER".equals(role)))
                .filter(response -> matchesSearch(response, term))
                .toList();
    }

    private boolean matchesSearch(UserResponse response, String term) {
        if (term.isBlank()) {
            return true;
        }

        String userId = response.getUserId() == null ? "" : response.getUserId().toString();
        String fullName = ((response.getFirstName() == null ? "" : response.getFirstName()) + " "
                + (response.getLastName() == null ? "" : response.getLastName())).toLowerCase();
        String username = response.getUsername() == null ? "" : response.getUsername().toLowerCase();
        String email = response.getEmail() == null ? "" : response.getEmail().toLowerCase();
        String access = String.join(" ", response.getRoles()).toLowerCase();

        return userId.contains(term)
                || fullName.contains(term)
                || username.contains(term)
                || email.contains(term)
                || access.contains(term);
    }

    private UserResponse toResponse(User user, List<String> roles) {
        return new UserResponse(
                user.getUserId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getEnabled(),
                roles
        );
    }
}
