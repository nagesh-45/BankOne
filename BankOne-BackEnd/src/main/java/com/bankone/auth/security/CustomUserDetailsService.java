package com.bankone.auth.security;

import com.bankone.role.AppAccess;
import com.bankone.user.entity.User;
import com.bankone.user.entity.UserRole;
import com.bankone.user.repository.UserRepository;
import com.bankone.user.repository.UserRoleRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    public CustomUserDetailsService(UserRepository userRepository, UserRoleRepository userRoleRepository) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with username: " + username));

        List<UserRole> userRoles = userRoleRepository.findByUserWithRole(user);
        Collection<? extends GrantedAuthority> authorities = buildAuthorities(userRoles);

        return new BankUserDetails(user, authorities);
    }

    private Collection<? extends GrantedAuthority> buildAuthorities(List<UserRole> userRoles) {
        Set<String> authorityValues = new HashSet<>();
        List<GrantedAuthority> authorities = new ArrayList<>();

        for (UserRole userRole : userRoles) {
            if (!Boolean.TRUE.equals(userRole.getActive()) || userRole.getRole() == null) {
                continue;
            }

            String roleName = userRole.getRole().getRoleName();
            String roleAuthority = "ROLE_" + roleName;
            if (authorityValues.add(roleAuthority)) {
                authorities.add(new SimpleGrantedAuthority(roleAuthority));
            }

            if (userRole.getRole().getAccessCodes() != null) {
                for (String code : userRole.getRole().getAccessCodes()) {
                    String accessAuthority = AppAccess.toAuthority(code);
                    if (authorityValues.add(accessAuthority)) {
                        authorities.add(new SimpleGrantedAuthority(accessAuthority));
                    }
                }
            }
        }

        return authorities;
    }
}
