package com.bankone.user.repository;

import com.bankone.user.entity.User;
import com.bankone.user.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    List<UserRole> findByUser(User user);

    @Query("""
            SELECT ur FROM UserRole ur
            JOIN FETCH ur.role
            WHERE ur.user = :user
            """)
    List<UserRole> findByUserWithRole(@Param("user") User user);

    @Query("""
            SELECT ur FROM UserRole ur
            JOIN FETCH ur.role
            JOIN FETCH ur.user
            WHERE ur.user.userId IN :userIds
              AND ur.active = true
            """)
    List<UserRole> findActiveByUserIds(@Param("userIds") List<Long> userIds);
}