package com.bankone.auth.service;

import org.springframework.security.authentication.LockedException;
import org.springframework.stereotype.Service;
import org.springframework.security.authentication.AuthenticationManager;
import com.bankone.user.repository.UserRepository;
import com.bankone.user.repository.UserRoleRepository;
import com.bankone.user.entity.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import com.bankone.auth.security.BankUserDetails;
import org.springframework.transaction.annotation.Transactional;
import com.bankone.auth.dto.ChangePasswordRequest;
import com.bankone.auth.dto.LoginResponse;
import com.bankone.auth.dto.UserProfileResponse;
import com.bankone.common.exception.BadRequestException;
import com.bankone.common.util.BusinessIdFormatter;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final JwtService jwtService;
    private final LoginAttemptService loginAttemptService;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationService(AuthenticationManager authenticationManager,
                                 UserRepository userRepository,
                                 UserRoleRepository userRoleRepository,
                                 JwtService jwtService,
                                 LoginAttemptService loginAttemptService,
                                 PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.jwtService = jwtService;
        this.loginAttemptService = loginAttemptService;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(String username, String password) {

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(username, password);

        User loginUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("Bad credentials"));

        if (Boolean.TRUE.equals(loginUser.getAccountLocked())) {
            throw new LockedException(
                    "Your account has been locked. Please visit your nearest branch.");
        }

        org.springframework.security.core.Authentication authenticatedUser;
        try {
            authenticatedUser = authenticationManager.authenticate(authentication);
        } catch (BadCredentialsException e) {
            loginAttemptService.incrementFailedAttempts(username);
            throw e;
        }
        BankUserDetails userDetails =
                (BankUserDetails) authenticatedUser.getPrincipal();

        String token = jwtService.generateToken(userDetails);
        List<String> roles = userDetails.getAuthorities().stream()
                .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                .toList();

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        user.setLastLogin(java.time.LocalDateTime.now());
        user.setFailedLoginAttempts(0);

        userRepository.save(user);

        return new LoginResponse(
                token,
                "Bearer",
                3600L,
                roles,
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail()
        );
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUserProfile() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        List<String> roles = userRoleRepository.findByUserWithRole(user).stream()
                .filter(userRole -> Boolean.TRUE.equals(userRole.getActive()))
                .map(userRole -> userRole.getRole().getRoleName())
                .toList();

        return new UserProfileResponse(
                user.getUserId(),
                BusinessIdFormatter.employeeCode(user.getUserId()),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getEnabled(),
                roles,
                user.getLastLogin()
        );
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        if (request.getNewPassword() == null || request.getNewPassword().isBlank()) {
            throw new BadRequestException("New password is required");
        }

        if (request.getNewPassword().length() < 8) {
            throw new BadRequestException("New password must be at least 8 characters");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("New password and confirm password do not match");
        }

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BadRequestException("New password must be different from the current password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangedAt(LocalDateTime.now());
        user.setFailedLoginAttempts(0);
        userRepository.save(user);
    }
}
