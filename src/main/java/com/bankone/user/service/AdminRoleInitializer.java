package com.bankone.user.service;

import com.bankone.role.entity.Role;
import com.bankone.role.repository.RoleRepository;
import com.bankone.user.entity.User;
import com.bankone.user.entity.UserRole;
import com.bankone.user.repository.UserRepository;
import com.bankone.user.repository.UserRoleRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminRoleInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    public AdminRoleInitializer(
            UserRepository userRepository,
            RoleRepository roleRepository,
            UserRoleRepository userRoleRepository
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        User adminUser = userRepository.findByUsername("admin").orElse(null);
        Role adminRole = roleRepository.findByRoleName("ADMIN").orElse(null);

        if (adminUser == null || adminRole == null) {
            return;
        }

        // Normalize placeholder admin display names (e.g. "hi" / "hello")
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
    }
}
