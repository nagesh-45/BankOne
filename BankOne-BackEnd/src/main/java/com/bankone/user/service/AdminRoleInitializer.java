package com.bankone.user.service;

import com.bankone.role.entity.Role;
import com.bankone.role.repository.RoleRepository;
import com.bankone.user.entity.User;
import com.bankone.user.entity.UserRole;
import com.bankone.user.repository.UserRepository;
import com.bankone.user.repository.UserRoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Ensures a bootstrap admin exists on fresh databases (e.g. Render Postgres).
 * Default credentials: admin / Admin@123 — change after first login.
 */
@Component
@Order(100)
public class AdminRoleInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminRoleInitializer.class);

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "Admin@123";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminRoleInitializer(
            UserRepository userRepository,
            RoleRepository roleRepository,
            UserRoleRepository userRoleRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Role adminRole = roleRepository.findByRoleName("ADMIN").orElse(null);
        if (adminRole == null) {
            log.warn("ADMIN role missing — skip admin bootstrap");
            return;
        }

        User adminUser = userRepository.findByUsername(ADMIN_USERNAME).orElse(null);
        if (adminUser == null) {
            adminUser = new User();
            adminUser.setUsername(ADMIN_USERNAME);
            adminUser.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));
            adminUser.setFirstName("System");
            adminUser.setLastName("Administrator");
            adminUser.setEmail("admin@bankone.local");
            adminUser.setEnabled(true);
            adminUser.setAccountLocked(false);
            adminUser.setCredentialsExpired(false);
            adminUser.setFailedLoginAttempts(0);
            adminUser.setPasswordChangedAt(LocalDateTime.now());
            adminUser = userRepository.save(adminUser);
            log.info("Created bootstrap admin user '{}'", ADMIN_USERNAME);
        } else {
            boolean nameNeedsFix =
                    "hi".equalsIgnoreCase(adminUser.getFirstName())
                            || "hello".equalsIgnoreCase(adminUser.getLastName())
                            || adminUser.getFirstName() == null
                            || adminUser.getFirstName().isBlank();

            if (nameNeedsFix) {
                adminUser.setFirstName("System");
                adminUser.setLastName("Administrator");
                userRepository.save(adminUser);
            }

            if (Boolean.TRUE.equals(adminUser.getAccountLocked())
                    || adminUser.getFailedLoginAttempts() != null
                    && adminUser.getFailedLoginAttempts() > 0) {
                adminUser.setAccountLocked(false);
                adminUser.setFailedLoginAttempts(0);
                userRepository.save(adminUser);
            }
        }

        UserRole existingAdminRole = userRoleRepository.findByUserWithRole(adminUser).stream()
                .filter(userRole ->
                        adminRole.getRoleId().equals(userRole.getRole().getRoleId())
                                && Boolean.TRUE.equals(userRole.getActive()))
                .findFirst()
                .orElse(null);

        if (existingAdminRole != null) {
            if (existingAdminRole.getRoleName() == null || existingAdminRole.getRoleName().isBlank()) {
                existingAdminRole.setRoleName(adminRole.getRoleName());
                userRoleRepository.save(existingAdminRole);
            }
            return;
        }

        UserRole userRole = new UserRole();
        userRole.setUser(adminUser);
        userRole.setRole(adminRole);
        userRole.setRoleName(adminRole.getRoleName());
        userRole.setActive(true);
        userRoleRepository.save(userRole);
        log.info("Assigned ADMIN role to '{}'", ADMIN_USERNAME);
    }
}
