package com.bankone.auth.controller;

import com.bankone.auth.service.AuthenticationService;
import com.bankone.auth.dto.ChangePasswordRequest;
import com.bankone.auth.dto.LoginRequest;
import com.bankone.auth.dto.LoginResponse;
import com.bankone.auth.dto.UserProfileResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return authenticationService.login(
                request.getUsername(),
                request.getPassword()
        );
    }

    @GetMapping("/me")
    public UserProfileResponse me() {
        return authenticationService.getCurrentUserProfile();
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authenticationService.changePassword(request);
        return ResponseEntity.noContent().build();
    }
}
