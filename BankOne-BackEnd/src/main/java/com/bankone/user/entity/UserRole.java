package com.bankone.user.entity;

import com.bankone.common.entity.AuditableEntity;
import com.bankone.role.entity.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
        name = "user_roles",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_role",
                        columnNames = {"user_id", "role_id"}
                )
        }
)
@SequenceGenerator(
        name = "user_role_seq",
        sequenceName = "user_role_seq",
        allocationSize = 1
)
public class UserRole extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_role_seq")
    @Column(name = "user_role_id")
    private Long userRoleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(name = "role_name", nullable = false, length = 50)
    private String roleName;

    @Column(name = "active", nullable = false)
    private Boolean active = true;
}