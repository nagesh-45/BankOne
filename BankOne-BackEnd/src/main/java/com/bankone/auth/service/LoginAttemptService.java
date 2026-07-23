package com.bankone.auth.service;

import com.bankone.user.entity.User;
import com.bankone.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginAttemptService {

    private final UserRepository userRepository;

    public LoginAttemptService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void incrementFailedAttempts(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            int attempts = user.getFailedLoginAttempts() == null
                    ? 0
                    : user.getFailedLoginAttempts();

            attempts++;

            user.setFailedLoginAttempts(attempts);

            if (attempts >= 5) {
                user.setAccountLocked(true);
            }

            userRepository.save(user);
        });
    }
}