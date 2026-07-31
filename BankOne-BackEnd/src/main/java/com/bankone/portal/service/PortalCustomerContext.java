package com.bankone.portal.service;

import com.bankone.common.exception.BadRequestException;
import com.bankone.user.entity.User;
import com.bankone.user.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class PortalCustomerContext {

    private final UserRepository userRepository;

    public PortalCustomerContext(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User currentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
    }

    public Long requireCustomerId() {
        User user = currentUser();
        if (user.getCustomerId() == null) {
            throw new BadRequestException("This login is not linked to a bank customer profile");
        }
        return user.getCustomerId();
    }
}
