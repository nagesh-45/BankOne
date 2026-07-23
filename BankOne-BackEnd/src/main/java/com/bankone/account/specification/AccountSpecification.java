package com.bankone.account.specification;

import com.bankone.account.entity.Account;
import com.bankone.common.util.BusinessIdFormatter;
import com.bankone.customer.entity.Customer;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

/**
 * Builds dynamic WHERE filters for GET /accounts?search=...
 * Same idea as CustomerSpecification.
 */
public final class AccountSpecification {

    private AccountSpecification() {
    }

    public static Specification<Account> matching(String search) {
        return (root, query, criteriaBuilder) -> {

            // Load customer with account (for mapping customerId safely).
            // Skip on COUNT queries used for pagination totals.
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("customer", JoinType.LEFT);
                query.distinct(true);
            }

            // No search text → match everything
            if (search == null || search.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            String trimmed = search.trim();

            // C00009 → filter by that customer
            Long customerCodedId = BusinessIdFormatter.parseCustomerId(trimmed);
            if (customerCodedId != null) {
                return criteriaBuilder.equal(
                        root.get("customer").get("customerId"),
                        customerCodedId
                );
            }

            // Long digit string → account number starts with ...
            if (trimmed.matches("\\d{6,}")) {
                return criteriaBuilder.like(root.get("accountNumber"), trimmed + "%");
            }

            // Plain number → customer id
            Long rawCustomerId = parseRawId(trimmed);
            if (rawCustomerId != null) {
                return criteriaBuilder.equal(
                        root.get("customer").get("customerId"),
                        rawCustomerId
                );
            }

            // Text match on several columns
            String value = "%" + trimmed.toLowerCase() + "%";
            Join<Account, Customer> customer = root.join("customer", JoinType.LEFT);

            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("accountNumber")), value),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("accountType")), value),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("branchCode")), value),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("status")), value),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("currencyCode")), value),
                    criteriaBuilder.like(criteriaBuilder.lower(customer.get("firstName")), value),
                    criteriaBuilder.like(criteriaBuilder.lower(customer.get("lastName")), value),
                    criteriaBuilder.like(criteriaBuilder.lower(customer.get("email")), value)
            );
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
}