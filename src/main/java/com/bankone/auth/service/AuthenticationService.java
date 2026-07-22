package com.bankone.auth.service;

import org.springframework.security.authentication.LockedException;
import org.springframework.stereotype.Service;
import org.springframework.security.authentication.AuthenticationManager;
import com.bankone.user.repository.UserRepository;
import com.bankone.user.entity.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import com.bankone.auth.security.BankUserDetails;
import org.springframework.transaction.annotation.Transactional;
import com.bankone.auth.dto.LoginResponse;
import org.springframework.security.authentication.BadCredentialsException;
import java.util.Optional;

@Service
public class AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final LoginAttemptService loginAttemptService;
    public AuthenticationService(AuthenticationManager authenticationManager,
                                 UserRepository userRepository,
                                 JwtService jwtService,
                                 LoginAttemptService loginAttemptService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.loginAttemptService = loginAttemptService;
    }


    public LoginResponse login(String username, String password) {

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(username, password);

        // Check if user exists and account is locked before authentication
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

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        user.setLastLogin(java.time.LocalDateTime.now());
        user.setFailedLoginAttempts(0);

        userRepository.save(user);

        return new LoginResponse(
                token,
                "Bearer",
                3600L
        );


    }
}