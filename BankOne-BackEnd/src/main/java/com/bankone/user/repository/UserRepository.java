package com.bankone.user.repository;

import com.bankone.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByEmailIgnoreCaseAndUserIdNot(String email, Long userId);

    @Query("""
            SELECT COUNT(DISTINCT u.userId)
            FROM User u
            JOIN UserRole ur ON ur.user = u
            JOIN ur.role r
            WHERE ur.active = true
              AND r.roleName IN ('ADMIN', 'EMPLOYEE', 'MANAGER')
            """)
    long countEmployees();
}