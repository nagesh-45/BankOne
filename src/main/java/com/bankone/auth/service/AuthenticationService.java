package com.bankone.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.security.authentication.AuthenticationManager;
import com.bankone.user.repository.UserRepository;
import com.bankone.user.entity.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import com.bankone.auth.security.BankUserDetails;
import org.springframework.transaction.annotation.Transactional;
import com.bankone.auth.dto.LoginResponse;

@Service
public class AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public AuthenticationService(AuthenticationManager authenticationManager,
                                 UserRepository userRepository,
                                 JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @Transactional
    public LoginResponse login(String username, String password) {

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(username, password);

        var authenticatedUser =
                authenticationManager.authenticate(authentication);

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