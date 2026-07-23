package com.bankone.role.entity;

import com.bankone.common.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
        name = "roles",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_roles_name", columnNames = "role_name")
        }
)
@SequenceGenerator(
        name = "role_seq",
        sequenceName = "role_seq",
        allocationSize = 1
)
public class Role extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "role_seq")
    @Column(name = "role_id")
    private Long roleId;

    @Column(name = "role_name", nullable = false, length = 50)
    private String roleName;

    @Column(name = "description", length = 255)
    private String description;
}