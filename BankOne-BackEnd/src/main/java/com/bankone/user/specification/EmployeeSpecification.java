package com.bankone.user.specification;

import com.bankone.common.util.BusinessIdFormatter;
import com.bankone.user.entity.User;
import com.bankone.user.entity.UserRole;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public final class EmployeeSpecification {

    private static final List<String> EMPLOYEE_ROLES = List.of("ADMIN", "EMPLOYEE", "MANAGER");

    private EmployeeSpecification() {
    }

    public static Specification<User> matching(String search) {
        return (root, query, criteriaBuilder) -> {
            Predicate isEmployee = existsActiveEmployeeRole(root, query, criteriaBuilder);

            if (search == null || search.isBlank()) {
                return isEmployee;
            }

            String trimmed = search.trim();

            // 2) Business code: E00001 / e00001 / E1
            Long codedId = BusinessIdFormatter.parseEmployeeId(trimmed);
            if (codedId != null) {
                return criteriaBuilder.and(
                        isEmployee,
                        criteriaBuilder.equal(root.get("userId"), codedId)
                );
            }

            // 3) Raw numeric id: 9
            Long rawId = parseRawId(trimmed);
            if (rawId != null) {
                return criteriaBuilder.and(
                        isEmployee,
                        criteriaBuilder.equal(root.get("userId"), rawId)
                );
            }

            String value = "%" + trimmed.toLowerCase() + "%";

            Subquery<Long> roleMatch = query.subquery(Long.class);
            Root<UserRole> roleRoot = roleMatch.from(UserRole.class);
            roleMatch.select(roleRoot.get("user").get("userId"))
                    .where(
                            criteriaBuilder.equal(
                                    roleRoot.get("user").get("userId"),
                                    root.get("userId")
                            ),
                            criteriaBuilder.isTrue(roleRoot.get("active")),
                            criteriaBuilder.like(
                                    criteriaBuilder.lower(roleRoot.get("roleName")),
                                    value
                            )
                    );

            Predicate textMatch = criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), value),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), value),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), value),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), value),
                    criteriaBuilder.exists(roleMatch)
            );

            return criteriaBuilder.and(isEmployee, textMatch);
        };
    }

    private static Long parseRawId(String value) {
        if (!value.matches("\\d+")) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static Predicate existsActiveEmployeeRole(
            Root<User> root,
            jakarta.persistence.criteria.CriteriaQuery<?> query,
            jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder
    ) {
        Subquery<Long> employeeRole = query.subquery(Long.class);
        Root<UserRole> userRoleRoot = employeeRole.from(UserRole.class);
        employeeRole.select(userRoleRoot.get("user").get("userId"))
                .where(
                        criteriaBuilder.equal(
                                userRoleRoot.get("user").get("userId"),
                                root.get("userId")
                        ),
                        criteriaBuilder.isTrue(userRoleRoot.get("active")),
                        userRoleRoot.get("roleName").in(EMPLOYEE_ROLES)
                );
        return criteriaBuilder.exists(employeeRole);
    }
}
