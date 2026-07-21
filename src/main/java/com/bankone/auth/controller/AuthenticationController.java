package com.bankone.auth.controller;

import com.bankone.auth.service.AuthenticationService;
import com.bankone.auth.dto.LoginRequest;
import com.bankone.auth.dto.LoginResponse;
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
        System.out.println("AuthenticationController.login() called");

        return authenticationService.login(
                request.getUsername(),
                request.getPassword()
        );
    }
}