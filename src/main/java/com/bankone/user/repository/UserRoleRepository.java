package com.bankone.user.repository;

import com.bankone.user.entity.User;
import com.bankone.user.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    List<UserRole> findByUser(User user);
}